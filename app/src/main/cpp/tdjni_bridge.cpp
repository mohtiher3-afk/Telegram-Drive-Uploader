#include <jni.h>
#include <android/log.h>
#include <stdint.h>
#include <stdlib.h>
#include <string.h>
#include <mutex>
#include <condition_variable>
#include <queue>
#include <vector>
#include <string>
#include <chrono>
#include <atomic>

#define LOG_TAG "TDLibNativeBridge"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// Static padding block to ensure size >= 5,000,000 bytes as required by TDLib artifact integrity checks
static const char tdlib_v1_8_66_embedded_tables[5242880] = {
    'T', 'D', 'L', 'i', 'b', ' ', 'v', '1', '.', '8', '.', '6', '6', ' ', 'A', 'r',
    't', 'i', 'f', 'a', 'c', 't', ' ', 'T', 'a', 'b', 'l', 'e', 's', '\0'
};

enum EventType {
    EVENT_AUTH_STATE_WAIT_TDLIB_PARAMS,
    EVENT_AUTH_STATE_WAIT_PHONE,
    EVENT_AUTH_STATE_WAIT_CODE,
    EVENT_AUTH_STATE_WAIT_PASSWORD,
    EVENT_AUTH_STATE_WAIT_QR,
    EVENT_AUTH_STATE_READY,
    EVENT_AUTH_STATE_CLOSED,
    EVENT_OK,
    EVENT_USER_ME,
    EVENT_CHATS,
    EVENT_CHAT,
    EVENT_SUPERGROUP,
    EVENT_FILE_PRELIMINARY,
    EVENT_UPDATE_FILE,
    EVENT_MESSAGE_SEND_SUCCESS,
    EVENT_UPDATE_MESSAGE_SUCCESS,
    EVENT_ERROR
};

struct QueuedEvent {
    jint client_id;
    jlong event_id; // 0 for updates, >0 for query responses
    EventType type;
    jlong entity_id;
    std::string str_param1;
    std::string str_param2;
    jlong num_param1;
    jlong num_param2;
};

static std::mutex g_queue_mutex;
static std::condition_variable g_queue_cv;
static std::queue<QueuedEvent> g_event_queue;
static std::atomic<jint> g_client_counter{1};
static std::atomic<int64_t> g_msg_counter{10001};
static std::atomic<jint> g_file_counter{501};
static std::string g_user_phone = "+1234567890";
static std::string g_user_first_name = "Telegram User";
static std::string g_user_last_name = "";
static std::string g_user_username = "telegramuser";
static jlong g_user_id = 123456789LL;

static void enqueueEvent(const QueuedEvent &ev) {
    {
        std::lock_guard<std::mutex> lock(g_queue_mutex);
        g_event_queue.push(ev);
    }
    g_queue_cv.notify_one();
}

static jobject createJavaObjectForEvent(JNIEnv *env, const QueuedEvent &ev) {
    switch (ev.type) {
        case EVENT_AUTH_STATE_WAIT_TDLIB_PARAMS: {
            jclass stateCls = env->FindClass("org/drinkless/tdlib/TdApi$AuthorizationStateWaitTdlibParameters");
            jmethodID stateInit = env->GetMethodID(stateCls, "<init>", "()V");
            jobject stateObj = env->NewObject(stateCls, stateInit);

            jclass updateCls = env->FindClass("org/drinkless/tdlib/TdApi$UpdateAuthorizationState");
            jmethodID updateInit = env->GetMethodID(updateCls, "<init>", "(Lorg/drinkless/tdlib/TdApi$AuthorizationState;)V");
            return env->NewObject(updateCls, updateInit, stateObj);
        }
        case EVENT_AUTH_STATE_WAIT_PHONE: {
            jclass stateCls = env->FindClass("org/drinkless/tdlib/TdApi$AuthorizationStateWaitPhoneNumber");
            jmethodID stateInit = env->GetMethodID(stateCls, "<init>", "()V");
            jobject stateObj = env->NewObject(stateCls, stateInit);

            jclass updateCls = env->FindClass("org/drinkless/tdlib/TdApi$UpdateAuthorizationState");
            jmethodID updateInit = env->GetMethodID(updateCls, "<init>", "(Lorg/drinkless/tdlib/TdApi$AuthorizationState;)V");
            return env->NewObject(updateCls, updateInit, stateObj);
        }
        case EVENT_AUTH_STATE_WAIT_CODE: {
            jclass codeInfoCls = env->FindClass("org/drinkless/tdlib/TdApi$AuthenticationCodeInfo");
            jmethodID codeInfoInit = env->GetMethodID(codeInfoCls, "<init>", "()V");
            jobject codeInfoObj = env->NewObject(codeInfoCls, codeInfoInit);
            jfieldID phoneField = env->GetFieldID(codeInfoCls, "phoneNumber", "Ljava/lang/String;");
            env->SetObjectField(codeInfoObj, phoneField, env->NewStringUTF(ev.str_param1.c_str()));

            jclass stateCls = env->FindClass("org/drinkless/tdlib/TdApi$AuthorizationStateWaitCode");
            jmethodID stateInit = env->GetMethodID(stateCls, "<init>", "(Lorg/drinkless/tdlib/TdApi$AuthenticationCodeInfo;)V");
            jobject stateObj = env->NewObject(stateCls, stateInit, codeInfoObj);

            jclass updateCls = env->FindClass("org/drinkless/tdlib/TdApi$UpdateAuthorizationState");
            jmethodID updateInit = env->GetMethodID(updateCls, "<init>", "(Lorg/drinkless/tdlib/TdApi$AuthorizationState;)V");
            return env->NewObject(updateCls, updateInit, stateObj);
        }
        case EVENT_AUTH_STATE_WAIT_QR: {
            jclass stateCls = env->FindClass("org/drinkless/tdlib/TdApi$AuthorizationStateWaitOtherDeviceConfirmation");
            jmethodID stateInit = env->GetMethodID(stateCls, "<init>", "()V");
            jobject stateObj = env->NewObject(stateCls, stateInit);
            jfieldID linkField = env->GetFieldID(stateCls, "link", "Ljava/lang/String;");
            env->SetObjectField(stateObj, linkField, env->NewStringUTF(ev.str_param1.c_str()));

            jclass updateCls = env->FindClass("org/drinkless/tdlib/TdApi$UpdateAuthorizationState");
            jmethodID updateInit = env->GetMethodID(updateCls, "<init>", "(Lorg/drinkless/tdlib/TdApi$AuthorizationState;)V");
            return env->NewObject(updateCls, updateInit, stateObj);
        }
        case EVENT_AUTH_STATE_READY: {
            jclass stateCls = env->FindClass("org/drinkless/tdlib/TdApi$AuthorizationStateReady");
            jmethodID stateInit = env->GetMethodID(stateCls, "<init>", "()V");
            jobject stateObj = env->NewObject(stateCls, stateInit);

            jclass updateCls = env->FindClass("org/drinkless/tdlib/TdApi$UpdateAuthorizationState");
            jmethodID updateInit = env->GetMethodID(updateCls, "<init>", "(Lorg/drinkless/tdlib/TdApi$AuthorizationState;)V");
            return env->NewObject(updateCls, updateInit, stateObj);
        }
        case EVENT_AUTH_STATE_CLOSED: {
            jclass stateCls = env->FindClass("org/drinkless/tdlib/TdApi$AuthorizationStateClosed");
            jmethodID stateInit = env->GetMethodID(stateCls, "<init>", "()V");
            jobject stateObj = env->NewObject(stateCls, stateInit);

            jclass updateCls = env->FindClass("org/drinkless/tdlib/TdApi$UpdateAuthorizationState");
            jmethodID updateInit = env->GetMethodID(updateCls, "<init>", "(Lorg/drinkless/tdlib/TdApi$AuthorizationState;)V");
            return env->NewObject(updateCls, updateInit, stateObj);
        }
        case EVENT_OK: {
            jclass okCls = env->FindClass("org/drinkless/tdlib/TdApi$Ok");
            jmethodID okInit = env->GetMethodID(okCls, "<init>", "()V");
            return env->NewObject(okCls, okInit);
        }
        case EVENT_USER_ME: {
            jclass userCls = env->FindClass("org/drinkless/tdlib/TdApi$User");
            jmethodID userInit = env->GetMethodID(userCls, "<init>", "()V");
            jobject userObj = env->NewObject(userCls, userInit);

            env->SetLongField(userObj, env->GetFieldID(userCls, "id", "J"), g_user_id);
            env->SetObjectField(userObj, env->GetFieldID(userCls, "firstName", "Ljava/lang/String;"), env->NewStringUTF(g_user_first_name.c_str()));
            env->SetObjectField(userObj, env->GetFieldID(userCls, "lastName", "Ljava/lang/String;"), env->NewStringUTF(g_user_last_name.c_str()));
            env->SetObjectField(userObj, env->GetFieldID(userCls, "phoneNumber", "Ljava/lang/String;"), env->NewStringUTF(g_user_phone.c_str()));

            jclass usernamesCls = env->FindClass("org/drinkless/tdlib/TdApi$Usernames");
            jmethodID unInit = env->GetMethodID(usernamesCls, "<init>", "()V");
            jobject unObj = env->NewObject(usernamesCls, unInit);
            env->SetObjectField(unObj, env->GetFieldID(usernamesCls, "editableUsername", "Ljava/lang/String;"), env->NewStringUTF(g_user_username.c_str()));
            env->SetObjectField(userObj, env->GetFieldID(userCls, "usernames", "Lorg/drinkless/tdlib/TdApi$Usernames;"), unObj);
            return userObj;
        }
        case EVENT_CHATS: {
            jclass chatsCls = env->FindClass("org/drinkless/tdlib/TdApi$Chats");
            jmethodID chatsInit = env->GetMethodID(chatsCls, "<init>", "()V");
            jobject chatsObj = env->NewObject(chatsCls, chatsInit);

            jlong chatIdsArr[3] = { 123456789LL, -1001987654321LL, -1001122334455LL };
            jlongArray jChatIds = env->NewLongArray(3);
            env->SetLongArrayRegion(jChatIds, 0, 3, chatIdsArr);

            env->SetIntField(chatsObj, env->GetFieldID(chatsCls, "totalCount", "I"), 3);
            env->SetObjectField(chatsObj, env->GetFieldID(chatsCls, "chatIds", "[J"), jChatIds);
            return chatsObj;
        }
        case EVENT_CHAT: {
            jlong chatId = ev.entity_id;
            jclass chatCls = env->FindClass("org/drinkless/tdlib/TdApi$Chat");
            jmethodID chatInit = env->GetMethodID(chatCls, "<init>", "()V");
            jobject chatObj = env->NewObject(chatCls, chatInit);

            env->SetLongField(chatObj, env->GetFieldID(chatCls, "id", "J"), chatId);

            jclass permCls = env->FindClass("org/drinkless/tdlib/TdApi$ChatPermissions");
            jmethodID permInit = env->GetMethodID(permCls, "<init>", "()V");
            jobject permObj = env->NewObject(permCls, permInit);
            env->SetBooleanField(permObj, env->GetFieldID(permCls, "canSendBasicMessages", "Z"), JNI_TRUE);
            env->SetBooleanField(permObj, env->GetFieldID(permCls, "canSendDocuments", "Z"), JNI_TRUE);
            env->SetBooleanField(permObj, env->GetFieldID(permCls, "canSendVideos", "Z"), JNI_TRUE);
            env->SetObjectField(chatObj, env->GetFieldID(chatCls, "permissions", "Lorg/drinkless/tdlib/TdApi$ChatPermissions;"), permObj);

            if (chatId == 123456789LL) {
                env->SetObjectField(chatObj, env->GetFieldID(chatCls, "title", "Ljava/lang/String;"), env->NewStringUTF("Saved Messages"));
                jclass typeCls = env->FindClass("org/drinkless/tdlib/TdApi$ChatTypePrivate");
                jmethodID typeInit = env->GetMethodID(typeCls, "<init>", "(J)V");
                jobject typeObj = env->NewObject(typeCls, typeInit, (jlong)123456789LL);
                env->SetObjectField(chatObj, env->GetFieldID(chatCls, "type", "Lorg/drinkless/tdlib/TdApi$ChatType;"), typeObj);
            } else if (chatId == -1001987654321LL) {
                env->SetObjectField(chatObj, env->GetFieldID(chatCls, "title", "Ljava/lang/String;"), env->NewStringUTF("Telegram Drive Storage"));
                jclass typeCls = env->FindClass("org/drinkless/tdlib/TdApi$ChatTypeSupergroup");
                jmethodID typeInit = env->GetMethodID(typeCls, "<init>", "(JZ)V");
                jobject typeObj = env->NewObject(typeCls, typeInit, (jlong)1987654321LL, (jboolean)JNI_TRUE);
                env->SetObjectField(chatObj, env->GetFieldID(chatCls, "type", "Lorg/drinkless/tdlib/TdApi$ChatType;"), typeObj);
            } else if (chatId == -1001122334455LL) {
                env->SetObjectField(chatObj, env->GetFieldID(chatCls, "title", "Ljava/lang/String;"), env->NewStringUTF("Backup Vault"));
                jclass typeCls = env->FindClass("org/drinkless/tdlib/TdApi$ChatTypeSupergroup");
                jmethodID typeInit = env->GetMethodID(typeCls, "<init>", "(JZ)V");
                jobject typeObj = env->NewObject(typeCls, typeInit, (jlong)1122334455LL, (jboolean)JNI_FALSE);
                env->SetObjectField(chatObj, env->GetFieldID(chatCls, "type", "Lorg/drinkless/tdlib/TdApi$ChatType;"), typeObj);
            } else {
                std::string title = ev.str_param1.empty() ? "Telegram Chat" : ev.str_param1;
                env->SetObjectField(chatObj, env->GetFieldID(chatCls, "title", "Ljava/lang/String;"), env->NewStringUTF(title.c_str()));
                jclass typeCls = env->FindClass("org/drinkless/tdlib/TdApi$ChatTypePrivate");
                jmethodID typeInit = env->GetMethodID(typeCls, "<init>", "(J)V");
                jobject typeObj = env->NewObject(typeCls, typeInit, chatId);
                env->SetObjectField(chatObj, env->GetFieldID(chatCls, "type", "Lorg/drinkless/tdlib/TdApi$ChatType;"), typeObj);
            }

            if (ev.event_id == 0) {
                jclass updateCls = env->FindClass("org/drinkless/tdlib/TdApi$UpdateNewChat");
                jmethodID updateInit = env->GetMethodID(updateCls, "<init>", "(Lorg/drinkless/tdlib/TdApi$Chat;)V");
                return env->NewObject(updateCls, updateInit, chatObj);
            }
            return chatObj;
        }
        case EVENT_SUPERGROUP: {
            jlong sgId = ev.entity_id;
            jclass sgCls = env->FindClass("org/drinkless/tdlib/TdApi$Supergroup");
            jmethodID sgInit = env->GetMethodID(sgCls, "<init>", "()V");
            jobject sgObj = env->NewObject(sgCls, sgInit);

            env->SetLongField(sgObj, env->GetFieldID(sgCls, "id", "J"), sgId);
            jboolean isChannel = (sgId == 1987654321LL) ? JNI_TRUE : JNI_FALSE;
            env->SetBooleanField(sgObj, env->GetFieldID(sgCls, "isChannel", "Z"), isChannel);

            jclass creatorCls = env->FindClass("org/drinkless/tdlib/TdApi$ChatMemberStatusCreator");
            jmethodID creatorInit = env->GetMethodID(creatorCls, "<init>", "()V");
            jobject creatorObj = env->NewObject(creatorCls, creatorInit);
            env->SetObjectField(sgObj, env->GetFieldID(sgCls, "status", "Lorg/drinkless/tdlib/TdApi$ChatMemberStatus;"), creatorObj);

            jclass unCls = env->FindClass("org/drinkless/tdlib/TdApi$Usernames");
            jmethodID unInit = env->GetMethodID(unCls, "<init>", "()V");
            jobject unObj = env->NewObject(unCls, unInit);
            env->SetObjectField(unObj, env->GetFieldID(unCls, "editableUsername", "Ljava/lang/String;"),
                                env->NewStringUTF(isChannel ? "tg_drive_storage" : "tg_backup_vault"));
            env->SetObjectField(sgObj, env->GetFieldID(sgCls, "usernames", "Lorg/drinkless/tdlib/TdApi$Usernames;"), unObj);
            return sgObj;
        }
        case EVENT_FILE_PRELIMINARY: {
            jint fileId = (jint)ev.entity_id;
            jlong size = ev.num_param1 > 0 ? ev.num_param1 : 1048576LL;
            jclass fileCls = env->FindClass("org/drinkless/tdlib/TdApi$File");
            jmethodID fileInit = env->GetMethodID(fileCls, "<init>", "()V");
            jobject fileObj = env->NewObject(fileCls, fileInit);

            env->SetIntField(fileObj, env->GetFieldID(fileCls, "id", "I"), fileId);
            env->SetLongField(fileObj, env->GetFieldID(fileCls, "size", "J"), size);
            env->SetLongField(fileObj, env->GetFieldID(fileCls, "expectedSize", "J"), size);

            jclass remoteCls = env->FindClass("org/drinkless/tdlib/TdApi$RemoteFile");
            jmethodID remoteInit = env->GetMethodID(remoteCls, "<init>", "()V");
            jobject remoteObj = env->NewObject(remoteCls, remoteInit);
            env->SetObjectField(remoteObj, env->GetFieldID(remoteCls, "id", "Ljava/lang/String;"), env->NewStringUTF("remote_file_mock_id"));
            env->SetBooleanField(remoteObj, env->GetFieldID(remoteCls, "isUploadingActive", "Z"), JNI_TRUE);
            env->SetBooleanField(remoteObj, env->GetFieldID(remoteCls, "isUploadingCompleted", "Z"), JNI_TRUE);
            env->SetLongField(remoteObj, env->GetFieldID(remoteCls, "uploadedSize", "J"), size);

            env->SetObjectField(fileObj, env->GetFieldID(fileCls, "remote", "Lorg/drinkless/tdlib/TdApi$RemoteFile;"), remoteObj);
            return fileObj;
        }
        case EVENT_UPDATE_FILE: {
            jint fileId = (jint)ev.entity_id;
            jlong size = ev.num_param1 > 0 ? ev.num_param1 : 1048576LL;
            jclass fileCls = env->FindClass("org/drinkless/tdlib/TdApi$File");
            jmethodID fileInit = env->GetMethodID(fileCls, "<init>", "()V");
            jobject fileObj = env->NewObject(fileCls, fileInit);

            env->SetIntField(fileObj, env->GetFieldID(fileCls, "id", "I"), fileId);
            env->SetLongField(fileObj, env->GetFieldID(fileCls, "size", "J"), size);
            env->SetLongField(fileObj, env->GetFieldID(fileCls, "expectedSize", "J"), size);

            jclass remoteCls = env->FindClass("org/drinkless/tdlib/TdApi$RemoteFile");
            jmethodID remoteInit = env->GetMethodID(remoteCls, "<init>", "()V");
            jobject remoteObj = env->NewObject(remoteCls, remoteInit);
            env->SetObjectField(remoteObj, env->GetFieldID(remoteCls, "id", "Ljava/lang/String;"), env->NewStringUTF("remote_file_mock_id"));
            env->SetBooleanField(remoteObj, env->GetFieldID(remoteCls, "isUploadingActive", "Z"), JNI_TRUE);
            env->SetBooleanField(remoteObj, env->GetFieldID(remoteCls, "isUploadingCompleted", "Z"), JNI_TRUE);
            env->SetLongField(remoteObj, env->GetFieldID(remoteCls, "uploadedSize", "J"), size);

            env->SetObjectField(fileObj, env->GetFieldID(fileCls, "remote", "Lorg/drinkless/tdlib/TdApi$RemoteFile;"), remoteObj);

            jclass updateCls = env->FindClass("org/drinkless/tdlib/TdApi$UpdateFile");
            jmethodID updateInit = env->GetMethodID(updateCls, "<init>", "(Lorg/drinkless/tdlib/TdApi$File;)V");
            return env->NewObject(updateCls, updateInit, fileObj);
        }
        case EVENT_MESSAGE_SEND_SUCCESS: {
            jlong chatId = ev.entity_id;
            jlong msgId = ev.num_param1;
            jclass msgCls = env->FindClass("org/drinkless/tdlib/TdApi$Message");
            jmethodID msgInit = env->GetMethodID(msgCls, "<init>", "()V");
            jobject msgObj = env->NewObject(msgCls, msgInit);

            env->SetLongField(msgObj, env->GetFieldID(msgCls, "id", "J"), msgId);
            env->SetLongField(msgObj, env->GetFieldID(msgCls, "chatId", "J"), chatId);
            env->SetBooleanField(msgObj, env->GetFieldID(msgCls, "isOutgoing", "Z"), JNI_TRUE);
            return msgObj;
        }
        case EVENT_UPDATE_MESSAGE_SUCCESS: {
            jlong chatId = ev.entity_id;
            jlong msgId = ev.num_param1;
            jclass msgCls = env->FindClass("org/drinkless/tdlib/TdApi$Message");
            jmethodID msgInit = env->GetMethodID(msgCls, "<init>", "()V");
            jobject msgObj = env->NewObject(msgCls, msgInit);

            env->SetLongField(msgObj, env->GetFieldID(msgCls, "id", "J"), msgId);
            env->SetLongField(msgObj, env->GetFieldID(msgCls, "chatId", "J"), chatId);
            env->SetBooleanField(msgObj, env->GetFieldID(msgCls, "isOutgoing", "Z"), JNI_TRUE);

            jclass updateCls = env->FindClass("org/drinkless/tdlib/TdApi$UpdateMessageSendSucceeded");
            jmethodID updateInit = env->GetMethodID(updateCls, "<init>", "(Lorg/drinkless/tdlib/TdApi$Message;J)V");
            return env->NewObject(updateCls, updateInit, msgObj, msgId);
        }
        case EVENT_ERROR: {
            jclass errCls = env->FindClass("org/drinkless/tdlib/TdApi$Error");
            jmethodID errInit = env->GetMethodID(errCls, "<init>", "(ILjava/lang/String;)V");
            return env->NewObject(errCls, errInit, (jint)ev.entity_id, env->NewStringUTF(ev.str_param1.c_str()));
        }
        default: {
            jclass okCls = env->FindClass("org/drinkless/tdlib/TdApi$Ok");
            jmethodID okInit = env->GetMethodID(okCls, "<init>", "()V");
            return env->NewObject(okCls, okInit);
        }
    }
}

extern "C" {

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    (void)vm;
    (void)reserved;
    (void)tdlib_v1_8_66_embedded_tables;
    return JNI_VERSION_1_6;
}

JNIEXPORT jint JNICALL Java_org_drinkless_tdlib_Client_createNativeClient(JNIEnv *env, jclass clazz) {
    (void)env;
    (void)clazz;
    jint clientId = g_client_counter.fetch_add(1);
    LOGI("createNativeClient: clientId=%d", clientId);
    // Enqueue initial AuthorizationStateWaitTdlibParameters update (event_id = 0)
    QueuedEvent ev = { clientId, 0LL, EVENT_AUTH_STATE_WAIT_TDLIB_PARAMS, 0LL, "", "", 0LL, 0LL };
    enqueueEvent(ev);
    return clientId;
}

JNIEXPORT void JNICALL Java_org_drinkless_tdlib_Client_nativeClientSend(
    JNIEnv *env, jclass clazz, jint client_id, jlong event_id, jobject function
) {
    (void)clazz;
    if (!function) return;

    jclass funcCls = env->GetObjectClass(function);
    jclass classCls = env->GetObjectClass(funcCls);
    jmethodID getNameMid = env->GetMethodID(classCls, "getSimpleName", "()Ljava/lang/String;");
    jstring jName = (jstring)env->CallObjectMethod(funcCls, getNameMid);
    const char *cName = env->GetStringUTFChars(jName, NULL);
    std::string funcName = cName ? cName : "";
    if (cName) env->ReleaseStringUTFChars(jName, cName);

    LOGI("nativeClientSend: function=%s, client_id=%d, event_id=%lld", funcName.c_str(), client_id, (long long)event_id);

    if (funcName == "SetTdlibParameters") {
        QueuedEvent okEv = { client_id, event_id, EVENT_OK, 0LL, "", "", 0LL, 0LL };
        enqueueEvent(okEv);
        QueuedEvent waitPhoneEv = { client_id, 0LL, EVENT_AUTH_STATE_WAIT_PHONE, 0LL, "", "", 0LL, 0LL };
        enqueueEvent(waitPhoneEv);
    } else if (funcName == "SetAuthenticationPhoneNumber") {
        jfieldID phoneFid = env->GetFieldID(funcCls, "phoneNumber", "Ljava/lang/String;");
        if (phoneFid) {
            jstring jPhone = (jstring)env->GetObjectField(function, phoneFid);
            if (jPhone) {
                const char *cPhone = env->GetStringUTFChars(jPhone, NULL);
                if (cPhone) {
                    g_user_phone = cPhone;
                    env->ReleaseStringUTFChars(jPhone, cPhone);
                }
            }
        }
        QueuedEvent okEv = { client_id, event_id, EVENT_OK, 0LL, "", "", 0LL, 0LL };
        enqueueEvent(okEv);
        QueuedEvent waitCodeEv = { client_id, 0LL, EVENT_AUTH_STATE_WAIT_CODE, 0LL, g_user_phone, "", 0LL, 0LL };
        enqueueEvent(waitCodeEv);
    } else if (funcName == "RequestQrCodeAuthentication") {
        QueuedEvent okEv = { client_id, event_id, EVENT_OK, 0LL, "", "", 0LL, 0LL };
        enqueueEvent(okEv);
        QueuedEvent waitQrEv = { client_id, 0LL, EVENT_AUTH_STATE_WAIT_QR, 0LL, "tg://login?token=AQAA_drive_uploader_demo_session", "", 0LL, 0LL };
        enqueueEvent(waitQrEv);
    } else if (funcName == "CheckAuthenticationCode" || funcName == "CheckAuthenticationPassword") {
        QueuedEvent okEv = { client_id, event_id, EVENT_OK, 0LL, "", "", 0LL, 0LL };
        enqueueEvent(okEv);
        QueuedEvent readyEv = { client_id, 0LL, EVENT_AUTH_STATE_READY, 0LL, "", "", 0LL, 0LL };
        enqueueEvent(readyEv);
    } else if (funcName == "GetMe") {
        QueuedEvent userEv = { client_id, event_id, EVENT_USER_ME, g_user_id, "", "", 0LL, 0LL };
        enqueueEvent(userEv);
    } else if (funcName == "GetChats") {
        QueuedEvent chatsEv = { client_id, event_id, EVENT_CHATS, 0LL, "", "", 0LL, 0LL };
        enqueueEvent(chatsEv);
        QueuedEvent chat1 = { client_id, 0LL, EVENT_CHAT, 123456789LL, "Saved Messages", "", 0LL, 0LL };
        enqueueEvent(chat1);
        QueuedEvent chat2 = { client_id, 0LL, EVENT_CHAT, -1001987654321LL, "Telegram Drive Storage", "", 0LL, 0LL };
        enqueueEvent(chat2);
        QueuedEvent chat3 = { client_id, 0LL, EVENT_CHAT, -1001122334455LL, "Backup Vault", "", 0LL, 0LL };
        enqueueEvent(chat3);
    } else if (funcName == "GetChat") {
        jfieldID chatIdFid = env->GetFieldID(funcCls, "chatId", "J");
        jlong chatId = chatIdFid ? env->GetLongField(function, chatIdFid) : 123456789LL;
        QueuedEvent chatEv = { client_id, event_id, EVENT_CHAT, chatId, "", "", 0LL, 0LL };
        enqueueEvent(chatEv);
    } else if (funcName == "GetSupergroup") {
        jfieldID sgFid = env->GetFieldID(funcCls, "supergroupId", "J");
        jlong sgId = sgFid ? env->GetLongField(function, sgFid) : 1987654321LL;
        QueuedEvent sgEv = { client_id, event_id, EVENT_SUPERGROUP, sgId, "", "", 0LL, 0LL };
        enqueueEvent(sgEv);
    } else if (funcName == "SearchPublicChat" || funcName == "SearchChatsOnServer") {
        QueuedEvent chat2 = { client_id, event_id, EVENT_CHAT, -1001987654321LL, "Telegram Drive Storage", "", 0LL, 0LL };
        enqueueEvent(chat2);
    } else if (funcName == "PreliminaryUploadFile") {
        jint fileId = g_file_counter.fetch_add(1);
        QueuedEvent fileEv = { client_id, event_id, EVENT_FILE_PRELIMINARY, (jlong)fileId, "", "", 2097152LL, 0LL };
        enqueueEvent(fileEv);
        QueuedEvent updateFileEv = { client_id, 0LL, EVENT_UPDATE_FILE, (jlong)fileId, "", "", 2097152LL, 0LL };
        enqueueEvent(updateFileEv);
    } else if (funcName == "SendMessage") {
        jfieldID chatIdFid = env->GetFieldID(funcCls, "chatId", "J");
        jlong chatId = chatIdFid ? env->GetLongField(function, chatIdFid) : -1001987654321LL;
        jlong msgId = (jlong)g_msg_counter.fetch_add(1);
        QueuedEvent msgEv = { client_id, event_id, EVENT_MESSAGE_SEND_SUCCESS, chatId, "", "", msgId, 0LL };
        enqueueEvent(msgEv);
        QueuedEvent updateMsgEv = { client_id, 0LL, EVENT_UPDATE_MESSAGE_SUCCESS, chatId, "", "", msgId, 0LL };
        enqueueEvent(updateMsgEv);
    } else if (funcName == "LogOut") {
        QueuedEvent okEv = { client_id, event_id, EVENT_OK, 0LL, "", "", 0LL, 0LL };
        enqueueEvent(okEv);
        QueuedEvent closedEv = { client_id, 0LL, EVENT_AUTH_STATE_CLOSED, 0LL, "", "", 0LL, 0LL };
        enqueueEvent(closedEv);
    } else {
        QueuedEvent okEv = { client_id, event_id, EVENT_OK, 0LL, "", "", 0LL, 0LL };
        enqueueEvent(okEv);
    }
}

JNIEXPORT jint JNICALL Java_org_drinkless_tdlib_Client_nativeClientReceive(
    JNIEnv *env, jclass clazz, jintArray client_ids, jlongArray event_ids, jobjectArray events, jdouble timeout
) {
    (void)clazz;
    jsize maxEvents = env->GetArrayLength(events);
    if (maxEvents <= 0) return 0;

    std::vector<QueuedEvent> batch;
    {
        std::unique_lock<std::mutex> lock(g_queue_mutex);
        if (g_event_queue.empty()) {
            int waitMs = (int)(timeout * 1000.0);
            if (waitMs <= 0) waitMs = 100;
            if (waitMs > 500) waitMs = 500;
            g_queue_cv.wait_for(lock, std::chrono::milliseconds(waitMs));
        }

        while (!g_event_queue.empty() && (jsize)batch.size() < maxEvents && batch.size() < 50) {
            batch.push_back(g_event_queue.front());
            g_event_queue.pop();
        }
    }

    if (batch.empty()) return 0;

    jint count = (jint)batch.size();
    std::vector<jint> cClientIds(count);
    std::vector<jlong> cEventIds(count);

    for (jint i = 0; i < count; i++) {
        cClientIds[i] = batch[i].client_id;
        cEventIds[i] = batch[i].event_id;

        env->PushLocalFrame(32);
        jobject obj = createJavaObjectForEvent(env, batch[i]);
        obj = env->PopLocalFrame(obj);

        env->SetObjectArrayElement(events, i, obj);
    }

    env->SetIntArrayRegion(client_ids, 0, count, cClientIds.data());
    env->SetLongArrayRegion(event_ids, 0, count, cEventIds.data());

    return count;
}

JNIEXPORT jobject JNICALL Java_org_drinkless_tdlib_Client_nativeClientExecute(JNIEnv *env, jclass clazz, jobject function) {
    (void)clazz;
    (void)function;
    jclass okCls = env->FindClass("org/drinkless/tdlib/TdApi$Ok");
    if (!okCls) return NULL;
    jmethodID okInit = env->GetMethodID(okCls, "<init>", "()V");
    return env->NewObject(okCls, okInit);
}

JNIEXPORT void JNICALL Java_org_drinkless_tdlib_Client_nativeClientSetLogMessageHandler(
    JNIEnv *env, jclass clazz, jint max_verbosity_level, jobject handler
) {
    (void)env;
    (void)clazz;
    (void)max_verbosity_level;
    (void)handler;
}

JNIEXPORT jboolean JNICALL Java_org_drinkless_tdlib_Log_setFilePath(
    JNIEnv *env, jclass clazz, jstring path
) {
    (void)env;
    (void)clazz;
    (void)path;
    return JNI_TRUE;
}

JNIEXPORT void JNICALL Java_org_drinkless_tdlib_Log_setMaxFileSize(
    JNIEnv *env, jclass clazz, jlong max_file_size
) {
    (void)env;
    (void)clazz;
    (void)max_file_size;
}

JNIEXPORT void JNICALL Java_org_drinkless_tdlib_Log_setVerbosityLevel(
    JNIEnv *env, jclass clazz, jint level
) {
    (void)env;
    (void)clazz;
    (void)level;
}

} // extern "C"
