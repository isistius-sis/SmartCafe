package com.example.smartcafe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.smartcafe.ui.theme.SmartCafeTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.material3.Button
import android.util.Patterns
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.layout.Arrangement

//测试邮箱：textext@email.com  密码：smartcafe

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val auth = FirebaseAuth.getInstance()
        //声明auth以调取登录功能？
        setContent {
            var isLoggedIn by remember { mutableStateOf(auth.currentUser != null) }
            //isLoggedIn初始为false，因此程序显示Login页面，登录成功后为true并切换页面
            //判断用户是否存在并持久化登录状态
            var showRegister by remember { mutableStateOf(false) }
            //判断当前是否应该显示注册页面，false显示login，true显示register
            when{
                isLoggedIn ->{
                    MenuScreen(
                        //如果已登录，则显示menu
                        onLogout = {
                            auth.signOut()
                            isLoggedIn = false
                        }
                    )
                }
                showRegister ->{
                    //未登录，但showRegister为true，则显示register
                    RegisterScreen(
                        auth = auth,
                        //与login共用FirebaseAuth实例
                        onBackToLogin = {
                            showRegister = false
                        },
                        onRegisterSuccess = {
                            isLoggedIn = true
                            //用户已登录，显示Menu
                            showRegister = false
                            //关闭Register页面
                        }
                    )
                }
                else ->{
                    LoginScreen(
                        //其他情况，显示login
                        auth = auth,
                        onLoginSuccess = {
                            isLoggedIn = true
                        },
                        onRegisterClick = {
                            showRegister = true
                        }
                    )
                }
            }
        }
    }
}

@Composable
//登录页
fun LoginScreen(
    auth: FirebaseAuth,
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit){

    //接受一个名为auth，类型为FirebaseAuth的对象
    //登录成功时调用onLoginSuccess
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }//保存错误信息
    var loginSucceeded by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier //外观布局修饰工具？
            .fillMaxSize() //让Column占据可用的最大空间
            .windowInsetsPadding(WindowInsets.safeDrawing) //增加边距避免与系统栏重叠
            .padding(24.dp), //增加内边距,dp为尺寸单位
        horizontalAlignment = Alignment.Start //控制Column水平排列位置（靠左）
    ) {
        Text(
            text = "Smart Cafe",
            fontSize = 28.sp //文字大小，sp为尺寸单位
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Login",
            fontSize = 24.sp
        )
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = email,//输入框显示内容
            onValueChange = { email = it },//输入内容保存
            label = {
                Text("Email Address")//输入框的标签
            }
        )
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = password,//输入框显示内容
            onValueChange = { password = it },//输入内容保存
            label = {
                Text("Password")//输入框的标签
            },
            visualTransformation = PasswordVisualTransformation() //密码隐藏
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            enabled = !isLoading,
            //修改 Button外观
            onClick = {
                loginSucceeded = false
                errorMessage = when { //根据条件判断执行
                    email.isBlank() ->
                        "Please enter your email."
                    //检查邮箱是否为空

                    !Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() ->
                        "Please enter a valid email address."
                    //检查输入内容是否符合邮箱格式

                    password.isBlank() ->
                        "Please enter your password."
                    //检查密码是否为空

                    else -> ""
                    //检查全部通过，清空之前报错
                }
                if (errorMessage.isEmpty()){
                    isLoading = true
                    //确认本地输入验证全部通过
                    auth.signInWithEmailAndPassword(email.trim(), password)
                        //接收参数（邮箱和密码）判断能否登录
                        .addOnCompleteListener { task ->
                            isLoading = false
                            //先验证，验证完成后再选择执行
                            if (task.isSuccessful) {
                                onLoginSuccess()
                            } else {
                                errorMessage = "Login failed. Please check your email and password."
                            }
                        }
                }//登录通过
            }
        ) {
            Text(text = if (isLoading) "Logging in..." else "Login")
            //加载时显示不同文字
        }
        if (errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                fontSize = 14.sp
            )
        }
        if (loginSucceeded){
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Login successful!",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 14.sp
            )
            //验证成功后显示login successful
            //后续这里替换为跳转至Menu
        }
        //这个没用了，但先留着

        Spacer(modifier = Modifier.height(16.dp))
        TextButton(
            onClick = onRegisterClick
        ){
            Text("Don't have an account? Register")
        }
    }
}

//商品信息模板
data class MenuItem(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val isAvailable: Boolean //有无货
)
@Composable
//菜单页
fun MenuScreen(onLogout: () -> Unit){
    //当用户需要退出登录时，执行onLogout操作
    var searchQuery by remember { mutableStateOf("") }
    //创建搜索
    val coffee01 = MenuItem(
        id = "coffee01",
        name = "咖啡01",
        description = "介绍01",
        price = 3.50,
        isAvailable = true
    )//使用之前的MenuItem模板，方便读取属性
    val coffee02 = MenuItem(
        id = "coffee02",
        name = "咖啡02",
        description = "介绍02",
        price = 3.80,
        isAvailable = true
    )
    val coffee03 = MenuItem(
        id = "coffee03",
        name = "咖啡03",
        description = "介绍03",
        price = 2.80,
        isAvailable = true
    )
    val coffee04 = MenuItem(
        id = "coffee04",
        name = "咖啡04",
        description = "介绍04",
        price = 4.20,
        isAvailable = true
    )
    val coffee05 = MenuItem(
        id = "coffee05",
        name = "咖啡05",
        description = "介绍05",
        price = 4.50,
        isAvailable = true
    )
    val coffee06 = MenuItem(
        id = "coffee06",
        name = "咖啡06",
        description = "介绍06",
        price = 2.00,
        isAvailable = true
    )

    val menuItems = listOf(
        coffee01,
        coffee02,
        coffee03,
        coffee04,
        coffee05,
        coffee06
    )
    //创建商品列表

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
    )
    {
        Text(
            text = "Smart Cafe Menu",
            fontSize = 28.sp
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search Menu") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        //搜索框
        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            //排成两列
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            //两列之间间隔 12dp
            verticalArrangement = Arrangement.spacedBy(12.dp)
            //不同行之间间隔 12dp
        ){
            items(menuItems) { menuItem ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = menuItem.name,
                            fontSize = 20.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = menuItem.description)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "£%.2f".format(menuItem.price))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (menuItem.isAvailable) "Available" else "Unavailable",
                            color = if (menuItem.isAvailable)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.error
                        )//供应状态

                    }
                }
            }
        }


        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onLogout
            //在点击按钮时执行onLogout操作
        ) {
            Text("Logout")
        }
    }
}

@Composable
//注册页
fun RegisterScreen(
    auth: FirebaseAuth,
    //接收一个FirebaseAuth对象
    onBackToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit
    //通知主页面注册成功
){
    var email by remember { mutableStateOf("") }
    //定义一个email变量
    var password by remember { mutableStateOf("") }
    //定义一个password变量
    var confirmPassword by remember { mutableStateOf("") }
    //定义二次确认密码的变量
    var errorMessage by remember { mutableStateOf("") }
    //此变量专门保存注册页的错误信息
    var isLoading by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(24.dp)
    ){
        Text(
            text = "Smart Cafe - Register",
            fontSize = 28.sp
        )
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = email,
            //输入框显示email的内容
            onValueChange = { email = it },
            //每次用户输入或删除字符都保存文本进email
            label = { Text("Email Address") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
            //输入框保持单行
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            //隐藏密码显示
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                errorMessage = when{
                    email.isBlank() -> "Please enter your email."
                    !Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() ->
                        "Please enter a valid email address."
                    //判断邮箱的基本结构
                    password.isBlank() -> "Please enter your password."
                    //用户先看到Please enter your password
                    password.length < 6 -> "Password must be at least 6 characters."
                    //判断密码长度,当密码字符数量少于6时，返回对应的错误信息。
                    confirmPassword.isBlank() -> "Please confirm your password."
                    //检查是否为空或只有空格
                    password != confirmPassword -> "Passwords do not match."
                    //判断两次输入的密码是否一致
                    else -> ""
                    //当三个输入框都有内容时，清除错误提示
                }
                if (errorMessage.isEmpty() && !isLoading){
                    //当本地输入验证全部通过时，才会发送Firebase注册请求
                    //&&避免注册进行中再次发送请求
                    isLoading = true
                    auth.createUserWithEmailAndPassword(
                        //创建邮箱密码账户
                        email.trim(),
                        password
                    ).addOnCompleteListener { task ->
                        isLoading = false
                        //返回结果后，无论成功还是失败，都恢复按钮状态
                        if (task.isSuccessful) {
                            onRegisterSuccess()
                            //返回结果为true时，进入menu
                        }else{
                            errorMessage = when (task.exception){
                                is FirebaseAuthUserCollisionException ->
                                    //判断 Firebase 返回的异常是否属于账户冲突类型
                                    "This email is already registered. Please log in."
                                else ->
                                    "Registration failed. Please try again."
                            }
                        }
                            //注册失败，显示错误信息
                    }
                }
            },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ){
            Text(if (isLoading) "Registering..." else "Register")
            //只有在没有进行注册请求时，按钮才允许点击
        }
        if (errorMessage.isNotEmpty()){
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = errorMessage,
                color = MaterialTheme.colorScheme.error)
        }
        //用户点击注册时，如果输入框为空，则显示提示

        TextButton(onClick = onBackToLogin) {
            Text("Back to Login")
        }
    }
}

