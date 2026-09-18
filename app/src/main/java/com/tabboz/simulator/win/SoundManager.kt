package com.tabboz.simulator.win

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log

/** Riproduzione dei suoni tabsNNNN.wav (asset wavs/). */
class SoundManager(private val context: Context) {
    private val pool: SoundPool = SoundPool.Builder()
        .setMaxStreams(4)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()
    private val loaded = HashMap<Int, Int>()   // numero suono -> soundId
    private val ready = HashSet<Int>()          // soundId caricati
    private val pending = HashSet<Int>()        // soundId da riprodurre appena pronti

    init {
        pool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) {
                ready.add(sampleId)
                if (pending.remove(sampleId)) pool.play(sampleId, 1f, 1f, 1, 0, 1f)
            }
        }
    }

    private fun soundIdFor(number: Int): Int? {
        loaded[number]?.let { return it }
        val name = "wavs/tabs%04d.wav".format(number)
        return try {
            val afd = context.assets.openFd(name)
            val id = pool.load(afd, 1)
            afd.close()
            loaded[number] = id
            id
        } catch (e: Exception) {
            Log.w("Tabboz", "Suono non trovato: $name")
            null
        }
    }

    fun preload(numbers: Collection<Int>) {
        for (n in numbers) soundIdFor(n)
    }

    fun play(number: Int) {
        val id = soundIdFor(number) ?: return
        if (id in ready) pool.play(id, 1f, 1f, 1, 0, 1f) else pending.add(id)
    }

    fun stopAll() {
        pending.clear()
        pool.autoPause()
    }
}
