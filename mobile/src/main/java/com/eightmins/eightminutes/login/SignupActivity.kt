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
import butterknife.Bind
import butterknife.ButterKnife
import butterknife.OnClick
import com.eightmins.eightminutes.MainActivity
import com.eightmins.eightminutes.R
import com.parse.ParseUser

class SignupActivity : AppCompatActivity() {

  @Bind(R.id.username) lateinit var username: EditText
  @Bind(R.id.password) lateinit var password: EditText
  @Bind(R.id.email) lateinit var email: EditText
  @Bind(R.id.phone) lateinit var phone: EditText

  override fun onCreate(savedInstanceState: Bundle?) {
    requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS)
    super.onCreate(savedInstanceState)

    setContentView(R.layout.activity_signup)
    ButterKnife.bind(this)
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    // Inflate the menu; this adds items to the action bar if it is present.
    menuInflater.inflate(R.menu.menu_signup, menu)
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    val id = item.itemId

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings) {
      return true
    }

    return super.onOptionsItemSelected(item)
  }

  @OnClick(R.id.button_login_sign_up)
  fun signUpClicked(view: View) {
    val username = this.username.text.toString().trim { it <= ' ' }
    val password = this.password.text.toString().trim { it <= ' ' }
    val email = this.email.text.toString().trim { it <= ' ' }
    val phone = this.phone.text.toString().trim { it <= ' ' }

    if (username.isEmpty()) {
      AlertDialog.Builder(this).setTitle(R.string.error_title).setMessage(R.string.username_cannot_be_empty).setPositiveButton(android.R.string.ok, null).create().show()
    } else if (password.isEmpty()) {
      AlertDialog.Builder(this).setTitle(R.string.error_title).setMessage(R.string.password_cannot_be_empty).setPositiveButton(android.R.string.ok, null).create().show()
    } else if (email.isEmpty()) {
      AlertDialog.Builder(this).setTitle(R.string.error_title).setMessage(R.string.email_cannot_be_empty).setPositiveButton(android.R.string.ok, null).create().show()
    } else if (email.isEmpty()) {
      AlertDialog.Builder(this).setTitle(R.string.error_title).setMessage(R.string.phone_cannot_be_empty).setPositiveButton(android.R.string.ok, null).create().show()
    } else {
      setProgressBarIndeterminate(true)

      val newUser = ParseUser()
      newUser.username = username
      newUser.setPassword(password)
      newUser.email = email
      newUser.put("phone", phone)
      newUser.signUpInBackground { exception ->
        setProgressBarIndeterminate(false)
        if (exception == null) {
          startActivity(Intent(this@SignupActivity, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
        } else {
          AlertDialog.Builder(this@SignupActivity).setTitle(R.string.error_title).setMessage(exception.message).setPositiveButton(android.R.string.ok, null).create().show()
        }
      }
    }
  }
}
