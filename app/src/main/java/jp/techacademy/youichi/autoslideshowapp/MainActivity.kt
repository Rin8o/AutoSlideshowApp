package jp.techacademy.youichi.autoslideshowapp

import android.Manifest
import android.support.v7.app.AppCompatActivity
import android.support.design.widget.Snackbar
import android.os.Bundle
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.provider.MediaStore
import android.content.ContentUris
import android.net.Uri
import android.os.Handler
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    private val PERMISSIONS_REQUEST_CODE = 100

    private var mTimer: Timer? = null
    // タイマー用の時間のための変数
    private var mTimerSec = 0.0
    private var mHandler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                getContentsInfo()
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_CODE)
            }
            // Android 5系以下の場合
        } else {
            getContentsInfo()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo()
                }
            else
                {
                    Snackbar.make(textView, "This app needs Permission.", Snackbar.LENGTH_SHORT).show()
                    // Snackbarに入力を促すメッセージを表示
                    requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_CODE)
                }
        }
    }

    private fun getContentsInfo() {
        // 画像の情報を取得する

        var num = 0
        var numMax = 0
        var mapID = LongArray(1000) // 画像のIDを格納するための配列　サイズ: 1000

        val resolver = contentResolver
        val cursor = resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
            null, // 項目(null = 全項目)
            null, // フィルタ条件(null = フィルタなし)
            null, // フィルタ用パラメータ
            null // ソート (null ソートなし)
        )

        if (cursor!!.moveToFirst()) {
            do {
                // indexからIDを取得し、そのIDから画像のURIを取得する
                val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                val id = cursor.getLong(fieldIndex)
                mapID[num] = id
                num++
            } while (cursor.moveToNext())
        }
        cursor.close()

        numMax=num
        num = 0

        val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, mapID[num])
        imageView.setImageURI(imageUri)
        textView.text="Photo Number: ${num+1} / $numMax \n"

        button1.setOnClickListener {
            if(num < numMax-1) {
                num++
                val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, mapID[num])
                imageView.setImageURI(imageUri)
                textView.text="Photo Number: ${num+1} / $numMax \n"
            }
            else {
                num = 0
                val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, mapID[num])
                imageView.setImageURI(imageUri)
                textView.text="Photo Number: ${num+1} / $numMax \n"
            }
        }

        button2.setOnClickListener {
            if(num > 0) {
                num--
                val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, mapID[num])
                imageView.setImageURI(imageUri)
                textView.text="Photo Number: ${num+1} / $numMax \n"
            }
            else {
                num = numMax-1
                val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, mapID[num])
                imageView.setImageURI(imageUri)
                textView.text="Photo Number: ${num+1} / $numMax \n"
            }
        }

        button3.setOnClickListener {
            if (mTimer == null) {
                // ボタンのクリックを無効にする
                button1.isClickable = false
                button1.text = ""
                button2.isClickable = false
                button2.text = ""

                mTimer = Timer()
                mTimer!!.schedule(object : TimerTask() {
                    override fun run() {
                        mHandler.post {
                            if (num < numMax - 1) {
                                num++
                                val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,mapID[num])
                                imageView.setImageURI(imageUri)
                                textView.text="Photo Number: ${num+1} / $numMax \n"
                                button3.text = "Stop"
                            }
                            else {
                                num = 0
                                val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,mapID[num])
                                imageView.setImageURI(imageUri)
                                textView.text="Photo Number: ${num+1} / $numMax \n"
                            }
                        }
                    }
                }, 2000, 2000) // 最初に始動させるまで 2000ミリ秒、ループの間隔を 2000ミリ秒 に設定
            }
            else {
                // ボタンのクリックを有効にする
                button1.isClickable = true
                button1.text = "Forward"
                button2.isClickable = true
                button2.text = "Backward"

                mTimer!!.cancel()
                mTimer = null
                button3.text = "Start"
            }
        }
    }
}