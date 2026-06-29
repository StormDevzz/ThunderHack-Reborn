#pragma once

#include <jni.h>

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jint JNICALL
Java_thunder_hack_core_manager_client_NativeOptimizer_nativeTrimMemory(JNIEnv*, jclass);

JNIEXPORT jint JNICALL
Java_thunder_hack_core_manager_client_NativeOptimizer_nativeSetHighPriority(JNIEnv*, jclass);

JNIEXPORT jlong JNICALL
Java_thunder_hack_core_manager_client_NativeOptimizer_nativeGetFreeMemory(JNIEnv*, jclass);

JNIEXPORT jstring JNICALL
Java_thunder_hack_core_manager_client_NativeOptimizer_nativeGetSystemInfo(JNIEnv*, jclass);

JNIEXPORT jint JNICALL
Java_thunder_hack_core_manager_client_NativeOptimizer_nativeOptimize(JNIEnv*, jclass);

JNIEXPORT jint JNICALL
Java_thunder_hack_core_manager_client_NativeOptimizer_nativeSetThreadAffinity(JNIEnv*, jclass);

JNIEXPORT jint JNICALL
Java_thunder_hack_core_manager_client_NativeOptimizer_nativeSetTimerResolution(JNIEnv*, jclass);

JNIEXPORT jint JNICALL
Java_thunder_hack_core_manager_client_NativeOptimizer_nativePreventPowerThrottling(JNIEnv*, jclass);

JNIEXPORT jint JNICALL
Java_thunder_hack_core_manager_client_NativeOptimizer_nativeSetMemoryPriority(JNIEnv*, jclass);

JNIEXPORT jint JNICALL
Java_thunder_hack_core_manager_client_NativeOptimizer_nativeEnableLowFragmentationHeap(JNIEnv*, jclass);

JNIEXPORT jint JNICALL
Java_thunder_hack_core_manager_client_NativeOptimizer_nativeDisableNagle(JNIEnv*, jclass);

JNIEXPORT jint JNICALL
Java_thunder_hack_core_manager_client_NativeOptimizer_nativeTrimWorkingSet(JNIEnv*, jclass);

JNIEXPORT jint JNICALL
Java_thunder_hack_core_manager_client_NativeOptimizer_nativeCompactHeap(JNIEnv*, jclass);

JNIEXPORT jint JNICALL
Java_thunder_hack_core_manager_client_NativeOptimizer_nativeClearSystemCaches(JNIEnv*, jclass);

JNIEXPORT jint JNICALL
Java_thunder_hack_core_manager_client_NativeOptimizer_nativeFullOptimize(JNIEnv*, jclass);

JNIEXPORT jstring JNICALL
Java_thunder_hack_core_manager_client_NativeOptimizer_nativeGetDetailedSystemInfo(JNIEnv*, jclass);

#ifdef __cplusplus
}
#endif
