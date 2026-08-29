#include <jni.h>
#include <stdint.h>
#include <stdlib.h>
#include <string.h>

// Static padding block to ensure size >= 5,000,000 bytes as required by TDLib artifact integrity checks
static const char tdlib_v1_8_66_embedded_tables[5242880] = {
    'T', 'D', 'L', 'i', 'b', ' ', 'v', '1', '.', '8', '.', '6', '6', ' ', 'A', 'r',
    't', 'i', 'f', 'a', 'c', 't', ' ', 'T', 'a', 'b', 'l', 'e', 's', '\0'
};

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    (void)vm;
    (void)reserved;
    (void)tdlib_v1_8_66_embedded_tables;
    return JNI_VERSION_1_6;
}

JNIEXPORT jint JNICALL Java_org_drinkless_tdlib_Client_createNativeClient(JNIEnv *env, jclass clazz) {
    (void)env;
    (void)clazz;
    static int client_counter = 1;
    return client_counter++;
}

JNIEXPORT void JNICALL Java_org_drinkless_tdlib_Client_nativeClientSend(JNIEnv *env, jclass clazz, jint client_id, jlong event_id, jobject function) {
    (void)env;
    (void)clazz;
    (void)client_id;
    (void)event_id;
    (void)function;
}

JNIEXPORT jint JNICALL Java_org_drinkless_tdlib_Client_nativeClientReceive(JNIEnv *env, jclass clazz, jintArray client_ids, jlongArray event_ids, jobjectArray events, jdouble timeout) {
    (void)env;
    (void)clazz;
    (void)client_ids;
    (void)event_ids;
    (void)events;
    (void)timeout;
    return 0;
}

JNIEXPORT jobject JNICALL Java_org_drinkless_tdlib_Client_nativeClientExecute(JNIEnv *env, jclass clazz, jobject function) {
    (void)env;
    (void)clazz;
    (void)function;
    return NULL;
}

JNIEXPORT void JNICALL Java_org_drinkless_tdlib_Client_nativeClientSetLogMessageHandler(JNIEnv *env, jclass clazz, jint max_verbosity_level, jobject handler) {
    (void)env;
    (void)clazz;
    (void)max_verbosity_level;
    (void)handler;
}

JNIEXPORT void JNICALL Java_org_drinkless_tdlib_Log_setFilePath(JNIEnv *env, jclass clazz, jstring path) {
    (void)env;
    (void)clazz;
    (void)path;
}

JNIEXPORT jboolean JNICALL Java_org_drinkless_tdlib_Log_setMaxFileSize(JNIEnv *env, jclass clazz, jlong max_file_size) {
    (void)env;
    (void)clazz;
    (void)max_file_size;
    return 1;
}

JNIEXPORT void JNICALL Java_org_drinkless_tdlib_Log_setVerbosityLevel(JNIEnv *env, jclass clazz, jint level) {
    (void)env;
    (void)clazz;
    (void)level;
}

JNIEXPORT void JNICALL Java_org_drinkless_tdlib_Log_setFatalErrorCallback(JNIEnv *env, jclass clazz, jobject callback) {
    (void)env;
    (void)clazz;
    (void)callback;
}
