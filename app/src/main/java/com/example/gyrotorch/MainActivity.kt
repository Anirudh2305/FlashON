package com.example.gyrotorch

import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import kotlin.math.pow
import kotlin.math.sqrt

class MainActivity : Activity(), SensorEventListener {

    private val shakeThreshold:Float= 50F
    private val minTime:Int =1000
    private var lastShakeTime:Long=0
    private var hasShaken:Boolean=false
    private lateinit var write:TextView


    private lateinit var sensorManager:SensorManager
    private var accMeter : Sensor?=null

    private lateinit var cameraManager: CameraManager
    private lateinit var cameraID: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
         write=findViewById(R.id.box)


        sensorManager=getSystemService(SENSOR_SERVICE) as SensorManager
        accMeter=sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        if(accMeter==null)
        {
            Toast.makeText(this,"Sensor not available",Toast.LENGTH_LONG).show()
            finish()
        }

        val isFlashAvailable = applicationContext.packageManager
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)

        cameraManager =getSystemService(CAMERA_SERVICE) as CameraManager
        cameraID = cameraManager.cameraIdList[0]

        if (!isFlashAvailable) {
            Toast.makeText(this,"Flashlight not available",Toast.LENGTH_LONG).show()
            finish()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)   //Requires API 23 and above for using camera2 feature.
    override fun onSensorChanged(event: SensorEvent?) {
        val currTime:Long=System.currentTimeMillis()
        if((currTime-lastShakeTime)>minTime)
        {
            val x = event!!.values[0]
            val y = event.values[1]
            val z = event.values[2]

            val acceleration= sqrt(x.toDouble().pow(2.0) + y.toDouble().pow(2.0) + z.toDouble().pow(2.0))-SensorManager.GRAVITY_EARTH
            if(acceleration>shakeThreshold)
            {
                lastShakeTime=currTime
                if(!hasShaken)
                {
                    window.decorView.setBackgroundColor(Color.GREEN)
                    cameraManager.setTorchMode(cameraID, true)
                    write.text=getString(R.string.statusON)  // can use written.text="Flashlight ON" also. This is just to not use hardcoded string.
                    hasShaken=true
                }
                else
                {
                    window.decorView.setBackgroundColor(Color.CYAN)
                    cameraManager.setTorchMode(cameraID, false)
                    write.text=getString(R.string.statusOFF)
                    hasShaken=false
                }
            }

        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Ignore
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(this,accMeter,SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
        Toast.makeText(this,"sensor not used anymore",Toast.LENGTH_LONG).show()
    }

}