package com.telegramdrive.uploader.tdlib

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.drinkless.tdlib.Client
import org.drinkless.tdlib.TdApi
import org.junit.Assert.assertNotNull
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicReference

@RunWith(AndroidJUnit4::class)
class TdLibRuntimeSmokeTest {
    @Test
    fun jniLoadsAndClientCreateSucceeds() {
        val tag = "TdLibRuntimeSmokeTest"
        val nativeFailure = AtomicReference<Throwable?>(null)

        try {
            System.loadLibrary("tdjni")
            Log.i(tag, "JNI_LOAD_STATUS=PASS")
        } catch (failure: Throwable) {
            Log.e(tag, "JNI_LOAD_STATUS=FAIL:${failure.javaClass.name}", failure)
            fail("System.loadLibrary(\"tdjni\") failed: ${failure.javaClass.name}: ${failure.message}")
        }

        var client: Client? = null
        try {
            client = Client.create(
                { update ->
                    if (update is TdApi.UpdateAuthorizationState) {
                        Log.i(
                            tag,
                            "AUTH_STATE=${update.authorizationState.javaClass.simpleName}"
                        )
                    }
                },
                { failure -> nativeFailure.compareAndSet(null, failure) },
                { failure -> nativeFailure.compareAndSet(null, failure) }
            )
            assertNotNull("TDLib Client.create() returned null", client)
            Log.i(tag, "CLIENT_CREATE_STATUS=PASS")
        } catch (failure: Throwable) {
            Log.e(tag, "CLIENT_CREATE_STATUS=FAIL:${failure.javaClass.name}", failure)
            fail("Client.create() failed: ${failure.javaClass.name}: ${failure.message}")
        } finally {
            client?.send(TdApi.Close(), null, null)
        }

        nativeFailure.get()?.let { failure ->
            fail("TDLib callback reported ${failure.javaClass.name}: ${failure.message}")
        }
    }
}
