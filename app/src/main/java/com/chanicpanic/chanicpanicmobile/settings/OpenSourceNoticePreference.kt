/*
 * Copyright (c) chanicpanic 2022
 */

package com.chanicpanic.chanicpanicmobile.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.AttributeSet
import android.webkit.WebView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import com.chanicpanic.chanicpanicmobile.R
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.google.android.gms.oss.licenses.R as lR


class OpenSourceNoticePreference(context: Context, attr: AttributeSet) : Preference(context, attr) {
    override fun onClick() {
        super.onClick()
        OssLicensesMenuActivity.setActivityTitle("Open Source Notices")
        context.startActivity(Intent(context, OssLicensesMenuActivity::class.java))
    }
}

open class LicensePreference(context: Context, attrs: AttributeSet) : Preference(context, attrs)

class LicenseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(lR.layout.libraries_social_licenses_license_activity)
        findViewById<TextView>(lR.id.license_activity_textview).text = intent.getStringExtra("license")
    }
}

class FlaskLicense(context: Context, attr : AttributeSet) : LicensePreference(context, attr) {
    override fun onClick() {
        super.onClick()
        val intent = Intent(context, LicenseActivity::class.java)
        intent.putExtra("license", context.getString(R.string.license_colorpicker))
        context.startActivity(intent)
    }
}

class MaterialLicense(context: Context, attr : AttributeSet) : LicensePreference(context, attr) {
    override fun onClick() {
        super.onClick()
        val intent = Intent(context, LicenseActivity::class.java)
        intent.putExtra("license", context.getString(R.string.license_material_icons))
        context.startActivity(intent)
    }
}

class PrivacyPreference(context: Context, attr: AttributeSet) : Preference(context, attr) {
    override fun onClick() {
        super.onClick()
        context.startActivity(Intent(context, WebActivity::class.java).apply { putExtra("uri", context.getString(
            R.string.privacy_uri
        )) })
    }
}

class TermsPreference(context: Context, attr: AttributeSet) : Preference(context, attr) {
    override fun onClick() {
        super.onClick()
        context.startActivity(Intent(context, WebActivity::class.java).apply { putExtra("uri", context.getString(
            R.string.terms_uri
        )) })
    }
}

class WebActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)
        val webView = findViewById<WebView>(R.id.web)
        webView.loadUrl(intent.getStringExtra("uri")!!)
    }
}
