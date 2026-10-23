package com.demo.photoalbumplayer

import androidx.activity.result.contract.ActivityResultContracts
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.demo.photoalbumplayer.databinding.ActivityMainBinding
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var mediaList = mutableListOf<Uri>()
    private var currentIndex = 0
    private var player: ExoPlayer? = null
    private var autoPlay = false
    private var playMode = PlayMode.LOOP
    private var animType = AnimType.FADE
    private var autoDelay = 3000L

    enum class PlayMode { LOOP, RANDOM }
    enum class AnimType { FADE, SLIDE }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSetting.setOnClickListener { showSettingDialog() }
        binding.btnPlaylist.setOnClickListener { showPlaylistDialog() }
        binding.btnVolume.setOnClickListener { showVolumeDialog() }

        binding.container.setOnClickListener { toggleAutoPlay() }
    }

    private fun toggleAutoPlay() {
        autoPlay = !autoPlay
        if (autoPlay) startAutoSlide()
        else stopAutoSlide()
    }

    private fun startAutoSlide() {
        binding.container.postDelayed({
            if (autoPlay) {
                nextItem()
                startAutoSlide()
            }
        }, autoDelay)
    }

    private fun stopAutoSlide() {
        binding.container.removeCallbacks(null)
    }

    private fun nextItem() {
        if (mediaList.isEmpty()) return
        currentIndex = when (playMode) {
            PlayMode.LOOP -> (currentIndex + 1) % mediaList.size
            PlayMode.RANDOM -> Random().nextInt(mediaList.size)
        }
        showItem(mediaList[currentIndex])
    }

    private fun prevItem() {
        if (mediaList.isEmpty()) return
        currentIndex = if (currentIndex - 1 < 0) mediaList.size - 1 else currentIndex - 1
        showItem(mediaList[currentIndex])
    }

    private fun showItem(uri: Uri) {
        player?.stop()
        val mimeType = contentResolver.getType(uri)
        if (mimeType?.startsWith("video/") == true) {
            playVideo(uri)
        } else if (mimeType?.startsWith("image/") == true) {
            showImage(uri)
        }
    }

    private fun showImage(uri: Uri) {
        val iv = ZoomImageView(this)
        iv.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        iv.setImageURI(uri)
        binding.container.removeAllViews()
        binding.container.addView(iv)
    }

    private fun playVideo(uri: Uri) {
        player = ExoPlayer.Builder(this).build()
        val mediaItem = MediaItem.fromUri(uri)
        player?.setMediaItem(mediaItem)
        player?.prepare()
        player?.play()
        val surfaceView = androidx.media3.ui.PlayerView(this)
        surfaceView.player = player
        surfaceView.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        binding.container.removeAllViews()
        binding.container.addView(surfaceView)
    }

    private fun showSettingDialog() {
        val v = LayoutInflater.from(this).inflate(R.layout.dialog_main_setting, null)
        val rgAnim = v.findViewById<RadioGroup>(R.id.rgAnim)
        when (animType) {
            AnimType.FADE -> rgAnim.check(R.id.rbFade)
            AnimType.SLIDE -> rgAnim.check(R.id.rbSlide)
        }
        AlertDialog.Builder(this)
            .setView(v)
            .show()
            .apply {
                v.findViewById<Button>(R.id.btnConfirm).setOnClickListener {
                    animType = if (rgAnim.checkedRadioButtonId == R.id.rbFade) AnimType.FADE else AnimType.SLIDE
                    dismiss()
                }
            }
    }

    private fun showPlaylistDialog() {
        val v = LayoutInflater.from(this).inflate(R.layout.dialog_playlist, null)
        val listView = v.findViewById<ListView>(R.id.listPlaylist)
        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mediaList.map { it.toString() })
        listView.adapter = adapter
        AlertDialog.Builder(this)
            .setView(v)
            .show()
            .apply {
                v.findViewById<Button>(R.id.btnClose).setOnClickListener { dismiss() }
                listView.onItemClickListener = { _, _, pos, _ ->
                    currentIndex = pos
                    showItem(mediaList[pos])
                    dis
