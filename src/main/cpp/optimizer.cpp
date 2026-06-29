#include "optimizer.h"

#ifdef _WIN32
  #define WIN32_LEAN_AND_MEAN
  #include <windows.h>
  #include <psapi.h>
  #include <heapapi.h>
  #include <avrt.h>
  #include <timeapi.h>
  #include <winsock2.h>
  #include <powrprof.h>
  #pragma comment(lib, "psapi.lib")
  #pragma comment(lib, "avrt.lib")
  #pragma comment(lib, "winmm.lib")
  #pragma comment(lib, "ws2_32.lib")
  #pragma comment(lib, "powrprof.lib")
#else
  #include <malloc.h>
  #include <unistd.h>
  #include <sys/resource.h>
  #include <sys/sysinfo.h>
  #include <sys/syscall.h>
  #include <sched.h>
  #include <pthread.h>
  #include <fstream>
  #include <thread>
  #include <chrono>
  #include <cstring>
#endif

#include <cstdio>
#include <string>
#include <cstdlib>

// =============================================================
// TRIM MEMORY
// =============================================================
JNIEXPORT jint JNICALL
Java_thunder_hack_core_manager_client_NativeOptimizer_nativeTrimMemory(JNIEnv*, jclass) {
    int result = 0;
#ifdef _WIN32
    HANDLE h = GetCurrentProcess();
    for (int i = 0; i < 3; i++) {
        if (SetProcessWorkingSetSize(h, (SIZE_T)-1, (SIZE_T)-1)) result++;
        Sleep(10);
    }
    if (_heapmin() == 0) result++;
#else
    for (int i = 0; i < 3; i++) {
        malloc_trim(0);
        std::this_thread::sleep_for(std::chrono::milliseconds(10));
        result++;
    }
    if (mallopt(M_TRIM_THRESHOLD, 1024 * 1024) == 1) result++;
#endif
    return result;
}

// =============================================================
// SET HIGH PRIORITY
// =============================================================
JNIEXPORT jint JNICALL
Java_thunder_hack_core_manager_client_NativeOptimizer_nativeSetHighPriority(JNIEnv*, jclass) {
    int result = 0;
#ifdef _WIN32
    HANDLE hProc = GetCurrentProcess();
    if (SetPriorityClass(hProc, HIGH_PRIORITY_CLASS)) result++;
    else if (SetPriorityClass(hProc, ABOVE_NORMAL_PRIORITY_CLASS)) result++;
    HANDLE hThread = GetCurrentThread();
    if (SetThreadPriority(hThread, THREAD_PRIORITY_TIME_CRITICAL)) result++;
#else
    if (setpriority(PRIO_PROCESS, 0, -15) == 0) result++;
    int ioprio = (1 << 13) | 0;
    syscall(SYS_ioprio_set, 1, 0, ioprio);
    pthread_t thread = pthread_self();
    int policy;
    struct sched_param param;
    if (pthread_getschedparam(thread, &policy, &param) == 0) {
        param.sched_priority = sched_get_priority_max(SCHED_RR);
        if (pthread_setschedparam(thread, SCHED_RR, &param) == 0) result++;
        else {
            param.sched_priority = sched_get_priority_max(SCHED_FIFO);
            if (pthread_setschedparam(thread, SCHED_FIFO, &param) == 0) result++;
        }
    }
#endif
    return result;
}

// =============================================================
// GET FREE MEMORY (KB)
// =============================================================
JNIEXPORT jlong JNICALL
Java_thunder_hack_core_manager_client_NativeOptimizer_nativeGetFreeMemory(JNIEnv*, jclass) {
#ifdef _WIN32
    MEMORYSTATUSEX ms;
    ms.dwLength = sizeof(ms);
    if (GlobalMemoryStatusEx(&ms)) return static_cast<jlong>(ms.ullAvailPhys / 1024);
    return -1;
#else
    std::ifstream f("/proc/meminfo");
    if (!f.is_open()) return -1;
    std::string line;
    while (std::getline(f, line)) {
        if (line.find("MemAvailable:") == 0) {
            unsigned long kb = 0;
            sscanf(line.c_str(), "MemAvailable: %lu kB", &kb);
            return static_cast<jlong>(kb);
        }
    }
    return -1;
#endif
}

// =============================================================
// GET SYSTEM INFO
// =============================================================
JNIEXPORT jstring JNICALL
Java_thunder_hack_core_manager_client_NativeOptimizer_nativeGetSystemInfo(JNIEnv* env, jclass) {
    char buf[512] = {0};
#ifdef _WIN32
    SYSTEM_INFO si; GetSystemInfo(&si);
    MEMORYSTATUSEX ms; ms.dwLength = sizeof(ms);
    uint64_t totalMB = 0;
    if (GlobalMemoryStatusEx(&ms)) totalMB = ms.ullTotalPhys / (1024 * 1024);
    snprintf(buf, sizeof(buf), "Windows | %u cores | %llu MB RAM", si.dwNumberOfProcessors, (unsigned long long)totalMB);
#else
    long cores = sysconf(_SC_NPROCESSORS_ONLN);
    long pages = sysconf(_SC_PHYS_PAGES);
    long pageSize = sysconf(_SC_PAGE_SIZE);
    long totalMB = (pages * pageSize) / (1024 * 1024);
    snprintf(buf, sizeof(buf), "Linux | %ld cores | %ld MB RAM", cores, totalMB);
#endif
    return env->NewStringUTF(buf);
}

// =============================================================
// QUICK OPTIMIZE (original)
// =============================================================
JNIEXPORT jint JNICALL
Java_thunder_hack_core_manager_client_NativeOptimizer_nativeOptimize(JNIEnv*, jclass) {
    jlong before = Java_thunder_hack_core_manager_client_NativeOptimizer_nativeGetFreeMemory(nullptr, nullptr);
    Java_thunder_hack_core_manager_client_NativeOptimizer_nativeTrimMemory(nullptr, nullptr);
    Java_thunder_hack_core_manager_client_NativeOptimizer_nativeSetHighPriority(nullptr, nullptr);
    jlong after = Java_thunder_hack_core_manager_client_NativeOptimizer_nativeGetFreeMemory(nullptr, nullptr);
    jint freed = 0;
    if (before >= 0 && after >= 0 && after > before) freed = static_cast<jint>(after - before);
    return freed;
}

// =============================================================
// SET THREAD AFFINITY — bind to performance cores
// =============================================================
JNIEXPORT jint JNICALL
Java_thunder_hack_core_manager_client_NativeOptimizer_nativeSetThreadAffinity(JNIEnv*, jclass) {
    int result = 0;
#ifdef _WIN32
    HANDLE hThread = GetCurrentThread();
    SYSTEM_INFO si; GetSystemInfo(&si);
    DWORD total = si.dwNumberOfProcessors;
    DWORD_PTR mask = 0;
    int limit = (total > 16) ? 12 : (total > 8) ? 8 : (int)total;
    for (int i = 0; i < limit; i++) mask |= (1ULL << i);
    if (SetThreadAffinityMask(hThread, mask)) result++;
    if (SetThreadPriority(hThread, THREAD_PRIORITY_TIME_CRITICAL)) result++;
    DWORD index = 0;
    HANDLE hAvrt = AvSetMmThreadCharacteristicsW(L"Games", &index);
    if (hAvrt) result++;
#else
    pthread_t thread = pthread_self();
    long numCPU = sysconf(_SC_NPROCESSORS_ONLN);
    if (numCPU > 0) {
        cpu_set_t cpuset;
        CPU_ZERO(&cpuset);
        int limit = (numCPU > 16) ? 12 : (numCPU > 8) ? 8 : (int)numCPU;
        for (int i = 0; i < limit; i++) CPU_SET(i, &cpuset);
        if (pthread_setaffinity_np(thread, sizeof(cpu_set_t), &cpuset) == 0) result++;
    }
    int policy;
    struct sched_param param;
    if (pthread_getschedparam(thread, &policy, &param) == 0) {
        param.sched_priority = sched_get_priority_max(SCHED_RR);
        if (pthread_setschedparam(thread, SCHED_RR, &param) == 0) result++;
    }
#endif
    return result;
}

// =============================================================
// SET TIMER RESOLUTION — 1ms precision
// =============================================================
JNIEXPORT jint JNICALL
Java_thunder_hack_core_manager_client_NativeOptimizer_nativeSetTimerResolution(JNIEnv*, jclass) {
    int result = 0;
#ifdef _WIN32
    TIMECAPS tc;
    if (timeGetDevCaps(&tc, sizeof(tc)) == TIMERR_NOERROR) {
        UINT resolution = (tc.wPeriodMin < 1) ? 1 : tc.wPeriodMin;
        if (timeBeginPeriod(resolution) == TIMERR_NOERROR) result++;
    }
#else
    result = 1;
#endif
    return result;
}

// =============================================================
// PREVENT POWER THROTTLING
// =============================================================
JNIEXPORT jint JNICALL
Java_thunder_hack_core_manager_client_NativeOptimizer_nativePreventPowerThrottling(JNIEnv*, jclass) {
    int result = 0;
#ifdef _WIN32
    typedef struct { ULONG Version, ControlMask, StateMask; } PTS;
    PTS pt; ZeroMemory(&pt, sizeof(pt));
    pt.Version = 1; pt.ControlMask = 1; pt.StateMask = 0;
    typedef BOOL(WINAPI* Sfn)(HANDLE, int, void*, DWORD);
    HMODULE hK = GetModuleHandleA("kernel32.dll");
    if (hK) {
        Sfn pfn = (Sfn)GetProcAddress(hK, "SetProcessInformation");
        if (pfn && pfn(GetCurrentProcess(), 4, &pt, sizeof(pt))) result++;
    }
    SetThreadExecutionState(ES_CONTINUOUS | ES_SYSTEM_REQUIRED);
    result++;
#else
    result = 1;
#endif
    return result;
}

JNIEXPORT jint JNICALL
Java_thunder_hack_core_manager_client_NativeOptimizer_nativeSetMemoryPriority(JNIEnv*, jclass) {
    int result = 0;
#ifdef _WIN32
    typedef struct { ULONG MemoryPriority; } Mpi;
    Mpi mpi; ZeroMemory(&mpi, sizeof(mpi));
    mpi.MemoryPriority = 5;
    typedef BOOL(WINAPI* Sfn)(HANDLE, int, void*, DWORD);
    HMODULE hK = GetModuleHandleA("kernel32.dll");
    if (hK) {
        Sfn pfn = (Sfn)GetProcAddress(hK, "SetProcessInformation");
        if (pfn && pfn(GetCurrentProcess(), 3, &mpi, sizeof(mpi))) result++;
    }
#else
    result = 1;
#endif
    return result;
}

JNIEXPORT jint JNICALL
Java_thunder_hack_core_manager_client_NativeOptimizer_nativeEnableLowFragmentationHeap(JNIEnv*, jclass) {
    int result = 0;
#ifdef _WIN32
    ULONG info = 2;
    if (HeapSetInformation(NULL, HeapCompatibilityInformation, &info, sizeof(info))) {
        HANDLE hHeap = GetProcessHeap();
        if (hHeap) HeapSetInformation(hHeap, HeapCompatibilityInformation, &info, sizeof(info));
        result++;
    }
#else
    result = 1;
#endif
    return result;
}

JNIEXPORT jint JNICALL
Java_thunder_hack_core_manager_client_NativeOptimizer_nativeDisableNagle(JNIEnv*, jclass) {
    int result = 0;
#ifdef _WIN32
    HMODULE hWI = LoadLibraryA("wininet.dll");
    if (hWI) {
        typedef BOOL(WINAPI* IsoFn)(HANDLE, DWORD, void*, DWORD);
        IsoFn pfn = (IsoFn)GetProcAddress(hWI, "InternetSetOptionA");
        if (pfn) { DWORD v = 1; pfn(NULL, 78, &v, sizeof(v)); result++; }
        FreeLibrary(hWI);
    }
    WSADATA wd;
    if (WSAStartup(MAKEWORD(2,2), &wd) == 0) {
        SOCKET s = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);
        if (s != INVALID_SOCKET) {
            int nd = 1;
            setsockopt(s, IPPROTO_TCP, TCP_NODELAY, (char*)&nd, sizeof(nd));
            closesocket(s);
            result++;
        }
        WSACleanup();
    }
#else
    result = 1;
#endif
    return result;
}

JNIEXPORT jint JNICALL
Java_thunder_hack_core_manager_client_NativeOptimizer_nativeTrimWorkingSet(JNIEnv*, jclass) {
    int result = 0;
#ifdef _WIN32
    HANDLE h = GetCurrentProcess();
    for (int i = 0; i < 3; i++) {
        if (SetProcessWorkingSetSize(h, (SIZE_T)-1, (SIZE_T)-1)) result++;
        if (EmptyWorkingSet(h)) result++;
        Sleep(10);
    }
#else
    for (int i = 0; i < 3; i++) {
        malloc_trim(0);
        std::this_thread::sleep_for(std::chrono::milliseconds(10));
        result++;
    }
#endif
    return result;
}

JNIEXPORT jint JNICALL
Java_thunder_hack_core_manager_client_NativeOptimizer_nativeCompactHeap(JNIEnv*, jclass) {
    int result = 0;
#ifdef _WIN32
    if (_heapmin() == 0) result++;
    if (SetProcessWorkingSetSize(GetCurrentProcess(), (SIZE_T)-1, (SIZE_T)-1)) result++;
#else
    if (mallopt(M_TRIM_THRESHOLD, 4096) == 1) result++;
    malloc_trim(0);
    result++;
#endif
    return result;
}

JNIEXPORT jint JNICALL
Java_thunder_hack_core_manager_client_NativeOptimizer_nativeClearSystemCaches(JNIEnv*, jclass) {
    int result = 0;
#ifndef _WIN32
    std::ifstream f("/proc/sys/vm/drop_caches");
    if (f.is_open()) {
        f.close();
        FILE* fp = fopen("/proc/sys/vm/drop_caches", "w");
        if (fp) {
            if (fputs("3\n", fp) != EOF) result++;
            fclose(fp);
        }
    }
    FILE* fp2 = fopen("/proc/sys/vm/compact_memory", "w");
    if (fp2) {
        if (fputs("1\n", fp2) != EOF) result++;
        fclose(fp2);
    }
#endif
    return result;
}

JNIEXPORT jint JNICALL
Java_thunder_hack_core_manager_client_NativeOptimizer_nativeFullOptimize(JNIEnv*, jclass) {
    int total = 0;
    total += Java_thunder_hack_core_manager_client_NativeOptimizer_nativeTrimWorkingSet(nullptr, nullptr);
    total += Java_thunder_hack_core_manager_client_NativeOptimizer_nativeCompactHeap(nullptr, nullptr);
    total += Java_thunder_hack_core_manager_client_NativeOptimizer_nativeSetHighPriority(nullptr, nullptr);
    total += Java_thunder_hack_core_manager_client_NativeOptimizer_nativeSetThreadAffinity(nullptr, nullptr);
    total += Java_thunder_hack_core_manager_client_NativeOptimizer_nativeSetTimerResolution(nullptr, nullptr);
    total += Java_thunder_hack_core_manager_client_NativeOptimizer_nativePreventPowerThrottling(nullptr, nullptr);
    total += Java_thunder_hack_core_manager_client_NativeOptimizer_nativeSetMemoryPriority(nullptr, nullptr);
    total += Java_thunder_hack_core_manager_client_NativeOptimizer_nativeEnableLowFragmentationHeap(nullptr, nullptr);
    total += Java_thunder_hack_core_manager_client_NativeOptimizer_nativeDisableNagle(nullptr, nullptr);
    total += Java_thunder_hack_core_manager_client_NativeOptimizer_nativeClearSystemCaches(nullptr, nullptr);
    return total;
}

JNIEXPORT jstring JNICALL
Java_thunder_hack_core_manager_client_NativeOptimizer_nativeGetDetailedSystemInfo(JNIEnv* env, jclass) {
    char buf[1024] = {0};
#ifdef _WIN32
    SYSTEM_INFO si; GetSystemInfo(&si);
    MEMORYSTATUSEX ms; ms.dwLength = sizeof(ms);
    uint64_t totalMB = 0, availMB = 0;
    if (GlobalMemoryStatusEx(&ms)) {
        totalMB = ms.ullTotalPhys / (1024 * 1024);
        availMB = ms.ullAvailPhys / (1024 * 1024);
    }
    snprintf(buf, sizeof(buf),
        "Windows | %u cores | %llu MB RAM (%llu free) | Priority: HIGH",
        si.dwNumberOfProcessors,
        (unsigned long long)totalMB,
        (unsigned long long)availMB);
#else
    long cores = sysconf(_SC_NPROCESSORS_ONLN);
    long pages = sysconf(_SC_PHYS_PAGES);
    long pageSize = sysconf(_SC_PAGE_SIZE);
    long totalMB = (pages * pageSize) / (1024 * 1024);
    struct sysinfo si;
    long freeMB = 0;
    if (sysinfo(&si) == 0) freeMB = si.freeram * si.mem_unit / (1024 * 1024);
    snprintf(buf, sizeof(buf),
        "Linux | %ld cores | %ld MB RAM (%ld free) | Priority: HIGH",
        cores, totalMB, freeMB);
#endif
    return env->NewStringUTF(buf);
}
