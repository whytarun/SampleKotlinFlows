package com.example.flowexample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*

class MainActivity : AppCompatActivity() {
    val channel = Channel<Int>()

    companion object {
        const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        producerForChannel()
        consumerForChannel()
        GlobalScope.launch {
            val data = producerFlow()
            data.collect {
                Log.d(TAG, "values==" + it)
            }
        }

        GlobalScope.launch {
            val data = producerFlow()
            data.collect {
                delay(3500)
                Log.d(TAG, "values2==" + it)
            }
        }

        /*
            we are using flow on for context switching so map & filter will run on Io thread
            because we have used flow-on operator. where as collect will run on main thread
         */
        GlobalScope.launch(Dispatchers.Main) {
            producerFlow1()
                .map {
                    delay(1000)
                    it * 2
                }
                .filter {
                    delay(1000)
                    it < 8
                }
                .flowOn(Dispatchers.IO)
                .collect {
                    Log.d(TAG, "values===" + it)
                }
        }

        /*
     we are using flow on for context switching so map on IO thread &  filter will run on Main thread
     because we have used flow-on operator. where as collect will run on main thread
  */
        GlobalScope.launch(Dispatchers.Main) {
            producerFlow1()
                .map {
                    delay(1000)
                    it * 2
                }
                .flowOn(Dispatchers.IO)
                .filter {
                    delay(1000)
                    it < 8
                }
                .flowOn(Dispatchers.Main)
                .collect {
                    Log.d(TAG, "values===" + it)
                }
        }

        /*
             everything will run on main thread
         */
        GlobalScope.launch(Dispatchers.Main) {
            producerFlow1()
                .map {
                    delay(1000)
                    it * 2
                }
                .filter {
                    delay(1000)
                    it < 8
                }
                .collect {
                    Log.d(TAG, "values===" + it)
                }
        }

        /*
            exception handling using try catch & with catch in producer
         */
        GlobalScope.launch(Dispatchers.Main) {
            try {

                producerFlowWithCatch()
                    .map {
                        delay(1000)
                        it * 2
                    }
                    .filter {
                        delay(1000)
                        it < 8
                    }
                    .collect {
                        Log.d(TAG, "values===" + it)
                    }
            } catch (e: Exception) {
                    Log.e(TAG, "ex..${e.message}")
            }
        }

        GlobalScope.launch {
            val data = mutuableSharedFlow()
            data.collect {
                delay(3500)
                Log.d(TAG, "values2==" + it)
            }
        }
    }

    private fun producerForChannel() {
        CoroutineScope(Dispatchers.Main).launch {
            channel.send(1)
            channel.send(2)
        }

    }

    private fun consumerForChannel() {
        CoroutineScope(Dispatchers.Main).launch {
            Log.e(TAG, "" + channel.receive())
            Log.e(TAG, "" + channel.receive())
        }
    }

    /*
        by default flow will create scopes for us
     */
    private fun producerFlow() = flow<Int> {
        val list = listOf<Int>(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
        list.forEach() {
            delay(1000)
            emit(it)
        }
    }

    private fun producerFlow1(): Flow<Int> {
        return flow<Int> {
            val list = listOf<Int>(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
            list.forEach() {
                delay(1000)
                emit(it)
            }
        }

    }

    private fun producerFlowWithCatch(): Flow<Int> {
        return flow<Int> {
            val list = listOf<Int>(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
            list.forEach() {
                delay(1000)
                emit(it)
            }
        }.catch {
            Log.d(TAG, " EXCEPTIONS===" + it.message)
            emit(-1)
        }

    }

    private fun mutuableSharedFlow() :Flow<Int>{
        val mutableSharedFlow =MutableSharedFlow<Int>()
        val list = listOf<Int>(1,3,5,7,8,9)
        GlobalScope.launch {
            list.forEach {
                mutableSharedFlow.emit(it)
            }

        }
        return mutableSharedFlow
    }
}