package com.eightmins.eightminutes.login

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.EditText

import com.eightmins.eightminutes.MainActivity
import com.eightmins.eightminutes.R
import com.parse.LogInCallback
import com.parse.ParseException
import com.parse.ParseUser

import butterknife.Bind
import butterknife.ButterKnife
import butterknife.OnClick

class LoginActivity : AppCompatActivity() {

  @Bind(R.id.username) lateinit var username: EditText
  @Bind(R.id.password) lateinit var password: EditText

  override fun onCreate(savedInstanceState: Bundle?) {
    requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS)
    super.onCreate(savedInstanceState)

    setContentView(R.layout.activity_login)
    ButterKnife.bind(this)
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    // Inflate the menu; this adds items to the action bar if it is present.
    menuInflater.inflate(R.menu.menu_login, menu)
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    val id = item.itemId

    if (id == R.id.action_settings) {
      return true
    }

    return super.onOptionsItemSelected(item)
  }

  @OnClick(R.id.button_login_sign_up)
  fun signUp(view: View) {
    startActivity(Intent(this, SignupActivity::class.java))
  }

  @OnClick(R.id.button_sign_in)
  fun signIn(view: View) {
    val username = this.username.text.toString().trim { it <= ' ' }
    val password = this.password.text.toString().trim { it <= ' ' }

    if (username.isEmpty()) {
      AlertDialog.Builder(this).setTitle(R.string.error_title).setMessage(R.string.username_cannot_be_empty).setPositiveButton(android.R.string.ok, null).create().show()
    } else if (password.isEmpty()) {
      AlertDialog.Builder(this).setTitle(R.string.error_title).setMessage(R.string.password_cannot_be_empty).setPositiveButton(android.R.string.ok, null).create().show()
    } else {
      setProgressBarIndeterminate(true)
      ParseUser.logInInBackground(username, password) { user, exception ->
        setProgressBarIndeterminate(false)
        if (exception == null) {
          startActivity(Intent(this@LoginActivity, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
        } else {
          AlertDialog.Builder(this@LoginActivity).setTitle(R.string.error_title).setMessage(exception.message).setPositiveButton(android.R.string.ok, null).create().show()
        }
      }
    }
  }
}
