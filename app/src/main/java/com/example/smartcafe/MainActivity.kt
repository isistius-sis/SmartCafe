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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.material3.AlertDialog
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.DisposableEffect
import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.border
import androidx.compose.material3.OutlinedButton
import androidx.compose.foundation.Image
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface





//测试邮箱：textext@email.com  密码：smartcafe
//测试邮箱2：text2@email.com  密码：cafecafe


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val auth = FirebaseAuth.getInstance()
        //声明auth以调取登录功能？身份认证
        val db = FirebaseFirestore.getInstance()
        //存储订单数据
        setContent {
            SmartCafeTheme {
                var isLoggedIn by remember {
                    mutableStateOf(auth.currentUser != null)
                }
                //isLoggedIn初始为false，因此程序显示Login页面，登录成功后为true并切换页面
                //判断用户是否存在并持久化登录状态
                var showRegister by remember { mutableStateOf(false) }
                //判断当前是否应该显示注册页面，false显示login，true显示register
                var showCart by remember { mutableStateOf(false) }
                //true显示购物车，false显示菜单
                var showOrders by remember { mutableStateOf(false) }
                //新增页面状态
                val cartItems = remember {
                    mutableStateListOf<CartItem>()
                }//保存购物车商品的列表
                when {
                    isLoggedIn -> {
                        val currentUserId = auth.currentUser?.uid
                        if (currentUserId != null) {
                            OrderStatusNotificationListener(
                                db = db,
                                userId = currentUserId
                            )
                        }
                        if (showOrders) {
                            MyOrdersScreen(
                                db = db,
                                userId = auth.currentUser?.uid.orEmpty(),
                                onBackToMenu = {
                                    showOrders = false
                                }
                            )
                        } else if (showCart) {
                            CartScreen(
                                cartItems = cartItems,
                                //左边是参数名，右边是购物车列表
                                onBackToMenu = {
                                    showCart = false
                                },
                                onIncrease = { cartItem ->
                                    val index = cartItems.indexOfFirst {
                                        it.menuItem.id == cartItem.menuItem.id
                                    }
                                    if (index >= 0) {
                                        val existingItem = cartItems[index]
                                        cartItems[index] = existingItem.copy(
                                            quantity = existingItem.quantity + 1
                                        )
                                    }//数量增加
                                },
                                onDecrease = { cartItem ->
                                    val index = cartItems.indexOfFirst {
                                        it.menuItem.id == cartItem.menuItem.id
                                    }
                                    if (index >= 0) {
                                        val existingItem = cartItems[index]
                                        if (existingItem.quantity > 1) {
                                            cartItems[index] = existingItem.copy(
                                                quantity = existingItem.quantity - 1
                                            )
                                        }
                                    }
                                },
                                onRemove = { cartItem ->
                                    val index = cartItems.indexOfFirst {
                                        it.menuItem.id == cartItem.menuItem.id
                                    }
                                    if (index >= 0) {
                                        cartItems.removeAt(index)
                                    }
                                },
                                onPlaceOrder = { onSuccess, onFailure ->
                                    Log.d("SmartCafeOrder", "Order submission started")
                                    val currentUser = auth.currentUser
                                    if (currentUser == null || cartItems.isEmpty()) {
                                        onFailure("Please log in and add products.")
                                    } else {
                                        val orderItems = cartItems.toList()
                                        val total = orderItems.sumOf {
                                            it.menuItem.price * it.quantity
                                        }
                                        val orderData = hashMapOf(
                                            "userId" to currentUser.uid,
                                            "status" to "Pending",
                                            "createdAt" to FieldValue.serverTimestamp(),
                                            "totalPrice" to total,
                                            "items" to orderItems.map { item ->
                                                hashMapOf(
                                                    "itemId" to item.menuItem.id,
                                                    "name" to item.menuItem.name,
                                                    "unitPrice" to item.menuItem.price,
                                                    "quantity" to item.quantity
                                                )
                                            }
                                        )
                                        db.collection("users")
                                            .document(currentUser.uid)
                                            .collection("orders")
                                            .add(orderData)
                                            .addOnSuccessListener { document ->
                                                Log.d(
                                                    "SmartCafeOrder",
                                                    "Order saved successfully: ${document.id}"
                                                )
                                                cartItems.clear()
                                                onSuccess(document.id)
                                            }
                                            .addOnFailureListener { exception ->
                                                Log.e(
                                                    "SmartCafeOrder",
                                                    "Order submission failed",
                                                    exception
                                                )
                                                onFailure(
                                                    exception.localizedMessage
                                                        ?: "Please try again."
                                                )
                                            }
                                    }
                                }
                            )
                        } else {
                            MenuScreen(
                                //如果已登录，则显示menu
                                onLogout = {
                                    auth.signOut()
                                    cartItems.clear()//Logout时清空购物车
                                    isLoggedIn = false
                                    showCart = false
                                    showOrders = false
                                },
                                onOpenCart = {
                                    showCart = true
                                },
                                //已登录时用showcart控制是否显示购物车
                                onOpenOrders = {
                                    showCart = false
                                    showOrders = true
                                },
                                onAddToCart = { menuItem ->
                                    if (menuItem.isAvailable) {
                                        val existingIndex = cartItems.indexOfFirst {
                                            //寻找已经存在的商品
                                            it.menuItem.id == menuItem.id
                                        }
                                        if (existingIndex >= 0) {
                                            val existingItem = cartItems[existingIndex]
                                            cartItems[existingIndex] =
                                                existingItem.copy(quantity = existingItem.quantity + 1)
                                            //替换购物车列表中的旧记录
                                        } else {
                                            cartItems.add(
                                                CartItem(
                                                    menuItem = menuItem,
                                                    quantity = 1
                                                )
                                            )
                                        }
                                    }
                                }
                            )
                        }
                    }

                    showRegister -> {
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

                    else -> {
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
        onRegisterClick: () -> Unit
    ) {

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
            /*Text(
                text = "Smart Cafe",
                fontSize = 28.sp //文字大小，sp为尺寸单位
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Login",
                fontSize = 24.sp
            )*/
            Text(
                text = "SMART CAFE",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Welcome back",
                fontSize = 27.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Sign in to continue your coffee break.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                    if (errorMessage.isEmpty()) {
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
                                    errorMessage =
                                        "Login failed. Please check your email and password."
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
            if (loginSucceeded) {
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
            ) {
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

    data class CartItem(
        val menuItem: MenuItem,
        val quantity: Int
    )

    //记录每件商品买了多少个
    fun getMenuItems(): List<MenuItem> {
        val coffee01 = MenuItem(
            id = "coffee01",
            name = "Coffee01",
            description = "Introduce01",
            price = 3.50,
            isAvailable = true
        )//使用之前的MenuItem模板，方便读取属性
        val coffee02 = MenuItem(
            id = "coffee02",
            name = "Coffee02",
            description = "Introduce02",
            price = 3.80,
            isAvailable = true
        )
        val coffee03 = MenuItem(
            id = "coffee03",
            name = "Coffee03",
            description = "Introduce03",
            price = 2.80,
            isAvailable = true
        )
        val coffee04 = MenuItem(
            id = "coffee04",
            name = "Coffee04",
            description = "Introduce04",
            price = 4.20,
            isAvailable = true
        )
        val coffee05 = MenuItem(
            id = "coffee05",
            name = "Coffee05",
            description = "Introduce05",
            price = 4.50,
            isAvailable = true
        )
        val coffee06 = MenuItem(
            id = "coffee06",
            name = "Coffee06",
            description = "Introduce06",
            price = 2.00,
            isAvailable = false
        )
        val cake01 = MenuItem(
            id = "cake01",
            name = "Cake01",
            description = "IntroduceIntroduceIntroduceIntroduceIntroduceIntroduceIntroduceIntroduceIntroduceIntroduce07",
            price = 4.50,
            isAvailable = true
        )
        val cake02 = MenuItem(
            id = "cake02",
            name = "Cake02",
            description = "Introduce08",
            price = 4.20,
            isAvailable = true
        )
        val cake03 = MenuItem(
            id = "cake03",
            name = "Cake03",
            description = "Introduce09",
            price = 3.90,
            isAvailable = true
        )
        val cake04 = MenuItem(
            id = "cake04",
            name = "Cake04",
            description = "Introduce Introduce Intro Introduce Introduce 10",
            price = 4.50,
            isAvailable = true
        )
        val product01 = MenuItem(
            id = "product01",
            name = "Product01",
            description = "Introduce11",
            price = 2.80,
            isAvailable = true
        )

        val product02 = MenuItem(
            id = "product02",
            name = "Product02",
            description = "Introduce12",
            price = 4.50,
            isAvailable = true
        )

        val product03 = MenuItem(
            id = "product03",
            name = "Product03",
            description = "Introduce13",
            price = 3.20,
            isAvailable = true
        )

        return listOf(
            coffee01,
            coffee02,
            coffee03,
            coffee04,
            coffee05,
            coffee06,
            cake01,
            cake02,
            cake03,
            cake04,
            product01,
            product02,
            product03
        )
        //创建商品列表
    }

    @Composable
//菜单页
    fun MenuScreen(
        onLogout: () -> Unit,
        onOpenCart: () -> Unit,
        onOpenOrders: () -> Unit,
        onAddToCart: (MenuItem) -> Unit
    ) {
        //当用户需要退出登录时，执行onLogout操作
        var searchQuery by remember { mutableStateOf("") }
        val menuItems = getMenuItems()
        //创建搜索
        val coffee01 = MenuItem(
            id = "coffee01",
            name = "Coffee01",
            description = "Introduce01",
            price = 3.50,
            isAvailable = true
        )//使用之前的MenuItem模板，方便读取属性
        val coffee02 = MenuItem(
            id = "coffee02",
            name = "Coffee02",
            description = "Introduce02",
            price = 3.80,
            isAvailable = true
        )
        val coffee03 = MenuItem(
            id = "coffee03",
            name = "Coffee03",
            description = "Introduce03",
            price = 2.80,
            isAvailable = true
        )
        val coffee04 = MenuItem(
            id = "coffee04",
            name = "Coffee04",
            description = "Introduce04",
            price = 4.20,
            isAvailable = true
        )
        val coffee05 = MenuItem(
            id = "coffee05",
            name = "Coffee05",
            description = "Introduce05",
            price = 4.50,
            isAvailable = true
        )
        val coffee06 = MenuItem(
            id = "coffee06",
            name = "Coffee06",
            description = "Introduce06",
            price = 2.00,
            isAvailable = false
        )
        val cake01 = MenuItem(
            id = "cake01",
            name = "Cake01",
            description = "IntroduceIntroduceIntroduceIntroduceIntroduceIntroduceIntroduceIntroduceIntroduceIntroduce07",
            price = 4.50,
            isAvailable = true
        )
        val cake02 = MenuItem(
            id = "cake02",
            name = "Cake02",
            description = "Introduce08",
            price = 4.20,
            isAvailable = true
        )
        val cake03 = MenuItem(
            id = "cake03",
            name = "Cake03",
            description = "Introduce09",
            price = 3.90,
            isAvailable = true
        )
        val cake04 = MenuItem(
            id = "cake04",
            name = "Cake04",
            description = "Introduce Introduce Intro Introduce Introduce 10",
            price = 4.50,
            isAvailable = true
        )
        val product01 = MenuItem(
            id = "product01",
            name = "Product01",
            description = "Introduce11",
            price = 2.80,
            isAvailable = true
        )

        val product02 = MenuItem(
            id = "product02",
            name = "Product02",
            description = "Introduce12",
            price = 4.50,
            isAvailable = true
        )

        val product03 = MenuItem(
            id = "product03",
            name = "Product03",
            description = "Introduce13",
            price = 3.20,
            isAvailable = true
        )


        val filteredItems = menuItems.filter { item ->
            item.name.contains(searchQuery, ignoreCase = true)
        }
        //筛选列表,contains检查是否包含指定文字

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 20.dp)
        )
        {
            /*Text(
                text = "Smart Cafe Menu",
                fontSize = 28.sp
            )
            Spacer(modifier = Modifier.height(16.dp))*/

            Text(
                text = "SMART CAFE",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Find your favourites",
                fontSize = 27.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Freshly picked for your coffee break.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))


            /*OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search Menu") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )*/
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text("Search drinks and treats")
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.fillMaxWidth()
            )
            //搜索框
            Spacer(modifier = Modifier.height(16.dp))


            Text(
                text = "OUR MENU  ·  ${filteredItems.size} items",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (filteredItems.isEmpty()) {
                Text(
                    text = "No products found.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            //如果isEmpty的结果为true，说明没有找到匹配的商品，则显示文字
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
            ) {
                items(filteredItems) { menuItem ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(288.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp)
                        ) {
                            val imageRes = when {
                                menuItem.id.startsWith("coffee") -> R.drawable.blackjava
                                menuItem.id.startsWith("cake") -> R.drawable.yellojava
                                else -> R.drawable.redjava
                            }

                            /*Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(68.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = when {
                                        menuItem.id.startsWith("coffee") -> "☕"
                                        menuItem.id.startsWith("cake") -> "🍰"
                                        else -> "🥐"
                                    },
                                    fontSize = 30.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))*/
                            Image(
                                painter = painterResource(id = imageRes),
                                contentDescription = menuItem.name,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(78.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = menuItem.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            /*Spacer(modifier = Modifier.height(8.dp))
                            Text(text = menuItem.description)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "£%.2f".format(menuItem.price))*/
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = menuItem.description,
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = "£%.2f".format(menuItem.price),
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (menuItem.isAvailable) "Available" else "Unavailable",
                                color = if (menuItem.isAvailable)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.error
                            )//供应状态，用colorScheme指示颜色

                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    onAddToCart(menuItem)
                                },
                                //点击按钮时执行括号内操作
                                enabled = menuItem.isAvailable,
                                //true时可以点击，false时按钮禁用
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(40.dp),
                                shape = RoundedCornerShape(20.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Text("Add", fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            Row(
                //内部组件横向排列
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(18.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(18.dp)
                    )
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
                //间隔距离6dp
            ) {
                Button(
                    onClick = onOpenCart,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    Text("Cart", fontSize = 12.sp)
                }
                OutlinedButton(
                    onClick = onOpenOrders,
                    modifier = Modifier
                        .weight(1.25f)
                        .height(44.dp),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    Text("My Orders", fontSize = 12.sp)
                }

                TextButton(
                    onClick = onLogout,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    Text("Logout", fontSize = 12.sp)//在点击按钮时执行onLogout操作
                }

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
    ) {
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
        ) {
            /*Text(
                text = "Smart Cafe - Register",
                fontSize = 28.sp
            )*/
            Text(
                text = "SMART CAFE",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Create your account",
                fontSize = 27.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Join us for your next coffee break.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                    errorMessage = when {
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
                    if (errorMessage.isEmpty() && !isLoading) {
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
                            } else {
                                errorMessage = when (task.exception) {
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
            ) {
                Text(if (isLoading) "Registering..." else "Register")
                //只有在没有进行注册请求时，按钮才允许点击
            }
            if (errorMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error
                )
            }
            //用户点击注册时，如果输入框为空，则显示提示

            TextButton(onClick = onBackToLogin) {
                Text("Back to Login")
            }
        }
    }

    @Composable
//购物车
    fun CartScreen(
        cartItems: List<CartItem>,
        onBackToMenu: () -> Unit,
        onIncrease: (CartItem) -> Unit,
        onDecrease: (CartItem) -> Unit,
        onRemove: (CartItem) -> Unit,
        onPlaceOrder: (
            onSuccess: (String) -> Unit,
            onFailure: (String) -> Unit
        ) -> Unit

    ) {
        val totalPrice = cartItems.sumOf { cartItem ->
            cartItem.menuItem.price * cartItem.quantity
        }//对列表中的数值求和，计算总价功能
        var showOrderReview by remember {
            mutableStateOf(false)
        }//增加弹窗状态
        var isPlacingOrder by remember {
            mutableStateOf(false)
        }//记录是否正在提交
        var orderError by remember {
            mutableStateOf("")
        }//记录错误信息
        var orderConfirmation by remember {
            mutableStateOf("")
        }//记录成功后的订单编号
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            /*Text("Shopping Cart")
            Text("Items in cart: ${cartItems.size}")
            Spacer(modifier = Modifier.height(12.dp))*/

            Text(
                text = "SMART CAFE",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Shopping Cart",
                fontSize = 27.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Items in cart: ${cartItems.size}",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(20.dp))
            //顶部标题
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                if (cartItems.isEmpty()) {
                    //如果购物车为空
                    Text("Your cart is empty.")
                } else {
                    //购物车不为空
                    cartItems.forEach { cartItem ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                                    shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer
                            )
                        ) {//先占满宽度，再预留空间
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = cartItem.menuItem.name,
                                        modifier = Modifier.weight(1f),
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "£%.2f".format(cartItem.menuItem.price),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                /*Row(//商品名称和价格放在同一行
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(cartItem.menuItem.name)
                                    Text("£%.2f".format(cartItem.menuItem.price))
                                }*/

                                /*Text(cartItem.menuItem.name)//商品名称
                            Text("£%.2f".format(cartItem.menuItem.price))//商品单价*/

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Quantity: ${cartItem.quantity}",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        /*Button(
                                            onClick = {
                                                onDecrease(cartItem)
                                            },
                                            enabled = cartItem.quantity > 1
                                        ) {
                                            Text("−")
                                        }*/
                                        OutlinedButton(
                                            onClick = {
                                                onDecrease(cartItem)
                                            },
                                            enabled = cartItem.quantity > 1,
                                            modifier = Modifier
                                                .width(48.dp)
                                                .height(40.dp),
                                            shape = RoundedCornerShape(12.dp),
                                            contentPadding = PaddingValues(0.dp)
                                        ) {
                                            Text("−", fontSize = 18.sp)
                                        }

                                        /*Button(
                                            onClick = {
                                                onIncrease(cartItem)
                                            }
                                        ) {
                                            Text("+")
                                        }//将quantity和+—放在同一行*/
                                        Button(
                                            onClick = {
                                                onIncrease(cartItem)
                                            },
                                            modifier = Modifier
                                                .width(48.dp)
                                                .height(40.dp),
                                            shape = RoundedCornerShape(12.dp),
                                            contentPadding = PaddingValues(0.dp)
                                        ) {
                                            Text("+", fontSize = 18.sp)
                                        }

                                    }
                                }
                                /*Text("Quantity: ${cartItem.quantity}")//购买数量
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = {
                                        onDecrease(cartItem)
                                    },
                                    enabled = cartItem.quantity > 1
                                ) {
                                    Text("−")
                                    //只有商品数量大于1时，减号按钮才能点击
                                }
                                Button(
                                    onClick = {
                                        onIncrease(cartItem)
                                    }
                                ) {
                                    Text("+")
                                }
                            }*/
                                /*Button(
                                    onClick = {
                                        onRemove(cartItem)
                                    }
                                ) {
                                    Text("Remove")
                                }*/
                                TextButton(
                                    onClick = {
                                        onRemove(cartItem)
                                    }
                                ) {
                                    Text(
                                        text = "Remove",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                                //彻底删除商品
                            }
                        }// 结束 Column
                    }// 结束 Card
                }
            }

            /*Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Total: £%.2f".format(totalPrice),//£%.2f格式化价格
                style = MaterialTheme.typography.titleMedium
            )//显示总价
            */
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                thickness = 1.dp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Order Total",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "£%.2f".format(totalPrice),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (orderConfirmation.isNotEmpty()) {
                Text(orderConfirmation)
            }

            Spacer(modifier = Modifier.height(12.dp))
            /*Button(
                onClick = {
                    showOrderReview = true
                },
                enabled = cartItems.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Review Order")
            }//查看订单，当购物车为空时无法使用
             */
            Button(
                onClick = {
                    showOrderReview = true
                },
                enabled = cartItems.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Review Order",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            /*Button(onClick = onBackToMenu) {
                Text("Back to Menu")
            }*/
            OutlinedButton(
                onClick = onBackToMenu,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Back to Menu",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        if (showOrderReview) {
            AlertDialog(
                onDismissRequest = {
                    if (!isPlacingOrder) {
                        showOrderReview = false
                    }
                },
                title = { Text("Review Your Order") },
                text = {
                    Column {
                        cartItems.forEach { cartItem ->
                            Text(
                                "${cartItem.menuItem.name} × ${cartItem.quantity}" + " - £%.2f".format(
                                    cartItem.menuItem.price * cartItem.quantity
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Total: £%.2f".format(totalPrice),
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (orderError.isNotEmpty()) {
                            Text(
                                text = orderError,
                                color = MaterialTheme.colorScheme.error
                            )
                        }//禁用按钮避免重复点击
                    }
                },
                confirmButton = {
                    TextButton(
                        enabled = !isPlacingOrder,
                        onClick = {
                            if (cartItems.isNotEmpty() && !isPlacingOrder) {
                                isPlacingOrder = true
                                orderError = ""
                                onPlaceOrder(
                                    { orderId ->
                                        isPlacingOrder = false
                                        showOrderReview = false
                                        orderConfirmation = "Order placed! ID: $orderId"
                                    },
                                    { error ->
                                        isPlacingOrder = false
                                        orderError = "Order failed: $error"
                                    }
                                )
                            }
                        }
                    ) {
                        Text(
                            if (isPlacingOrder) "Placing..."
                            else "Place Order"
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        enabled = !isPlacingOrder,
                        onClick = {
                            showOrderReview = false
                        }
                    ) {
                        Text("Back to Cart")
                    }
                }
            )
        }//订单展示弹窗
    }

    data class OrderLine(
        val itemId: String,
        val name: String,
        val unitPrice: Double?,
        val quantity: Int?
    )

    data class OrderSummary(
        val id: String,
        val status: String,
        val totalPrice: Double?,
        val createdAt: Date?,
        val items: List<OrderLine>,
        val reviewRating: Int? = null,
        val reviewComment: String? = null, //已提交的文字评价
        val reviewedAt: Date? = null //评价提交时间

    )//展示历史订单

    @Composable
    fun MyOrdersScreen(
        db: FirebaseFirestore,
        userId: String,
        onBackToMenu: () -> Unit
    ) {
        var orders by remember(userId) {
            mutableStateOf<List<OrderSummary>>(emptyList())
        }
        var isLoading by remember(userId) {
            mutableStateOf(true)
        }
        var errorMessage by remember(userId) {
            mutableStateOf("")
        }
        var selectedOrder by remember(userId) {
            mutableStateOf<OrderSummary?>(null)
        }
        var reviewOrder by remember(userId) {
            mutableStateOf<OrderSummary?>(null)
        }//正在评价哪笔订单
        var reviewRating by remember(userId) {
            mutableStateOf(0)
        }//打分分数
        var reviewComment by remember(userId) {
            mutableStateOf("")
        }//评价内容
        var isSubmittingReview by remember(userId) {
            mutableStateOf(false)
        }//提交按钮Loading
        var reviewError by remember(userId) {
            mutableStateOf("")
        }//失败提示
        /*val context = LocalContext.current
    var notificationMessage by remember {
        mutableStateOf("")
    }
    val notificationPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { granted ->

            notificationMessage = if (granted) {
                val posted = showOrderStatusNotification(
                    context = context,
                    orderId = "TEST",
                    status = "Preparing"
                )

                if (posted) {
                    "Test notification posted. Check the notification shade."
                } else {
                    "Notifications are disabled in system settings."
                }
            } else {
                "Notification permission was not granted."
            }
        }*/
        val context = LocalContext.current
        val notificationManager = context.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager
        var hasNotificationPermission by remember {
            mutableStateOf(
                Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                        context.checkSelfPermission(
                            Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED
            )
        }
        var notificationMessage by remember {
            mutableStateOf("")
        }
        val notificationPermissionLauncher =
            rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { granted ->
                hasNotificationPermission = granted

                notificationMessage = if (granted) {
                    "Order notifications enabled."
                } else {
                    "Notification permission was not granted."
                }
            }
        DisposableEffect(userId, db) {
            val registration = if (userId.isBlank()) {
                orders = emptyList()
                errorMessage = "Please log in to view your orders."
                isLoading = false
                null
            } else {
                isLoading = true
                errorMessage = ""
                db.collection("users")
                    .document(userId)
                    .collection("orders")
                    .orderBy(
                        "createdAt",
                        Query.Direction.DESCENDING
                    )
                    .addSnapshotListener { snapshot, exception -> //在订单数据发生变化时接收新的查询
                        if (exception != null) {
                            orders = emptyList()
                            selectedOrder = null
                            errorMessage = "Unable to load orders: " +
                                    (exception.localizedMessage
                                        ?: "Please try again.")
                            isLoading = false
                        } else if (snapshot != null) {
                            val updatedOrders = snapshot.documents.map { document ->
                                OrderSummary(
                                    id = document.id,
                                    status = document.getString("status")
                                        ?: "Unknown",
                                    totalPrice = document.getDouble("totalPrice"),
                                    createdAt = document
                                        .getTimestamp("createdAt")
                                        ?.toDate(),
                                    items = (document.get("items") as? List<*>)
                                        ?.mapNotNull { rawItem ->
                                            val fields = rawItem as? Map<*, *>
                                                ?: return@mapNotNull null
                                            OrderLine(
                                                itemId = fields["itemId"] as? String
                                                    ?: "Unknown",
                                                name = fields["name"] as? String
                                                    ?: "Unknown item",
                                                unitPrice = (fields["unitPrice"] as? Number)
                                                    ?.toDouble(),
                                                quantity = (fields["quantity"] as? Number)
                                                    ?.toInt()
                                            )
                                        } ?: emptyList(),
                                    reviewRating = document.getLong("reviewRating")?.toInt(),
                                    reviewComment = document.getString("reviewComment"),
                                    reviewedAt = document.getTimestamp("reviewedAt")?.toDate()
                                    //读取订单状态和评价状态。
                                )
                            }
                            orders = updatedOrders
                            selectedOrder = selectedOrder?.let { previous ->
                                updatedOrders.firstOrNull {
                                    it.id == previous.id
                                }
                            }
                            errorMessage = ""
                            isLoading = false
                        }
                    }
            }
            onDispose {
                registration?.remove() //解除Firestore监听?
            }
        }
        /*LaunchedEffect(userId) {
        if (userId.isBlank()) {
            errorMessage = "Please log in to view your orders."
            isLoading = false
        } else {
            isLoading = true
            errorMessage = ""

            db.collection("users")
                .document(userId)
                .collection("orders")
                .orderBy(
                    "createdAt",
                    Query.Direction.DESCENDING
                )
                .get()
                .addOnSuccessListener { snapshot ->
                    orders = snapshot.documents.map { document ->
                        OrderSummary(
                            id = document.id,
                            status = document.getString("status")
                                ?: "Unknown",
                            totalPrice = document.getDouble("totalPrice"),
                            createdAt = document
                                .getTimestamp("createdAt")
                                ?.toDate(),

                            items = (document.get("items") as? List<*>)
                                ?.mapNotNull { rawItem ->
                                    val fields = rawItem as? Map<*, *>
                                        ?: return@mapNotNull null

                                    OrderLine(
                                        itemId = fields["itemId"] as? String ?: "Unknown",
                                        name = fields["name"] as? String ?: "Unknown item",
                                        unitPrice = (fields["unitPrice"] as? Number)?.toDouble(),
                                        quantity = (fields["quantity"] as? Number)?.toInt()
                                    )
                                } ?: emptyList()
                        )
                    }
                    isLoading = false
                }
                .addOnFailureListener { exception ->
                    errorMessage = "Unable to load orders: " +
                            (exception.localizedMessage
                                ?: "Please try again.")
                    isLoading = false
                }
        }
    }*/

        val dateFormatter = remember {
            SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.UK)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Text(
                text = "SMART CAFE",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "My Orders",
                fontSize = 27.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Track your orders and past purchases.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when {
                    isLoading -> {
                        CircularProgressIndicator()
                    }

                    errorMessage.isNotEmpty() -> {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    orders.isEmpty() -> {
                        Text("No orders yet.")
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(orders.size) { index ->
                                val order = orders[index]

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 12.dp)
                                        .clickable {
                                            selectedOrder = order
                                        },
                                    shape = RoundedCornerShape(18.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp)
                                    ) {
                                        /*Text(
                                            text = "Order ID: ${order.id}",
                                            style = MaterialTheme.typography.titleSmall
                                        )*/
                                        Text(
                                            text = "Order #${order.id.take(8)}",
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "ID: ${order.id}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(
                                            modifier = Modifier.height(8.dp)
                                        )
                                        Text("Status: ${order.status}")
                                        val dateText = order.createdAt?.let {
                                            dateFormatter.format(it)
                                        } ?: "Time unavailable"
                                        /*Text("Placed: $dateText")*/
                                        Text(
                                            text = "Placed: $dateText",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        val totalText = order.totalPrice?.let {
                                            String.format(
                                                Locale.UK,
                                                "£%.2f",
                                                it
                                            )
                                        } ?: "Unavailable"
                                        /*Text("Total: $totalText")*/
                                        Spacer(modifier = Modifier.height(10.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Total",
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )

                                            Text(
                                                text = totalText,
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }//突出显示金额
                                    }
                                }
                            }
                        }
                    }
                }
            }

            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "ORDER NOTIFICATIONS",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(10.dp))


            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainer
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    /*TextButton(
            onClick = {
                if (
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    context.checkSelfPermission(
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    notificationPermissionLauncher.launch(
                        Manifest.permission.POST_NOTIFICATIONS
                    )
                } else {
                    val posted = showOrderStatusNotification(
                        context = context,
                        orderId = "TEST",
                        status = "Preparing"
                    )

                    notificationMessage = if (posted) {
                        "Test notification posted. Check the notification shade."
                    } else {
                        "Notifications are disabled in system settings."
                    }
                }
            }
        ) {
            Text("Send Test Notification")
        }
        if (notificationMessage.isNotEmpty()) {
            Text(
                text = notificationMessage,
                style = MaterialTheme.typography.bodySmall
            )
        }*/
                    if (
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        !hasNotificationPermission
                    ) {
                        /*TextButton(
                            onClick = {
                                notificationPermissionLauncher.launch(
                                    Manifest.permission.POST_NOTIFICATIONS
                                )
                            }
                        ) {
                            Text("Enable Order Notifications")
                        }*/
                        OutlinedButton(
                            onClick = {
                                notificationPermissionLauncher.launch(
                                    Manifest.permission.POST_NOTIFICATIONS
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(
                                text = "Enable Order Notifications",
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        /*
                        Text(
                            text = if (notificationManager.areNotificationsEnabled()) {
                                "Order notifications enabled."
                            } else {
                                "Notifications are disabled in Android settings."
                            },
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    if (notificationMessage.isNotEmpty()) {
                        Text(
                            text = notificationMessage,
                            style = MaterialTheme.typography.bodySmall
                        )*/
                        val notificationsEnabled =
                            notificationManager.areNotificationsEnabled()
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ){
                            Text(
                                text = if (notificationsEnabled) "●" else "○",
                                fontSize = 20.sp,
                                color = if (notificationsEnabled)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (notificationsEnabled)
                                    "Order notifications enabled."
                                else
                                    "Notifications are disabled in Android settings.",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            /*Button(onClick = onBackToMenu) {
                Text("Back to Menu")
            }*/
            OutlinedButton(
                onClick = onBackToMenu,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Back to Menu",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            /*Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onBackToMenu) {
            Text("Back to Menu")
        }*/
        }
        selectedOrder?.let { order ->
            AlertDialog(
                onDismissRequest = {
                    selectedOrder = null
                },
                title = {
                    Text("Order Details")
                },
                text = {
                    Column(
                        modifier = Modifier
                            .heightIn(max = 400.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text("Order ID: ${order.id}")
                        /*Text("Status: ${order.status}")*/
                        val statusBackground = when (order.status) {
                            "Completed" -> MaterialTheme.colorScheme.primary
                            "Ready for Pickup" -> MaterialTheme.colorScheme.primaryContainer
                            "Preparing" -> MaterialTheme.colorScheme.secondaryContainer
                            else -> MaterialTheme.colorScheme.surfaceContainerHigh
                        }
                        val statusTextColor = when (order.status) {
                            "Completed" -> MaterialTheme.colorScheme.onPrimary
                            "Ready for Pickup" -> MaterialTheme.colorScheme.onPrimaryContainer
                            "Preparing" -> MaterialTheme.colorScheme.onSecondaryContainer
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                        Surface(
                            color = statusBackground,
                            shape = RoundedCornerShape(50.dp)
                        ) {
                            Text(
                                text = order.status,
                                color = statusTextColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(
                                    horizontal = 12.dp,
                                    vertical = 6.dp
                                )
                            )
                        }//pending白色，preparing浅绿，ready绿色，completed深绿
                        Spacer(modifier = Modifier.height(16.dp))

                        if (order.items.isEmpty()) {
                            Text("Item details unavailable.")
                        } else {
                            order.items.forEach { item ->

                                val unitText = item.unitPrice?.let {
                                    String.format(Locale.UK, "£%.2f", it)
                                } ?: "Unavailable"

                                val subtotalText = item.unitPrice?.let { price ->
                                    item.quantity?.let { quantity ->
                                        String.format(
                                            Locale.UK,
                                            "£%.2f",
                                            price * quantity
                                        )
                                    }
                                } ?: "Unavailable"

                                Text(
                                    text = item.name,
                                    style = MaterialTheme.typography.titleMedium
                                )

                                Text("Item ID: ${item.itemId}")
                                Text("Quantity: ${item.quantity ?: "Unavailable"}")
                                Text("Unit price: $unitText")
                                Text("Subtotal: $subtotalText")

                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                        val totalText = order.totalPrice?.let {
                            String.format(Locale.UK, "£%.2f", it)
                        } ?: "Unavailable"
                        Text(
                            text = "Total: $totalText",
                            style = MaterialTheme.typography.titleMedium
                        )

                        if (order.reviewRating != null) {

                            Spacer(modifier = Modifier.height(20.dp))

                            Text(
                                text = "Your Review",
                                style = MaterialTheme.typography.titleMedium
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            val rating = order.reviewRating

                            Text(
                                text = "★".repeat(rating.coerceIn(0, 5)) +
                                        "☆".repeat((5 - rating).coerceIn(0, 5)) +
                                        " ($rating/5)",
                                style = MaterialTheme.typography.titleMedium
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = order.reviewComment
                                    ?.takeIf { it.isNotBlank() }
                                    ?: "No written comment."
                            )

                            order.reviewedAt?.let { date ->
                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Reviewed: ${dateFormatter.format(date)}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }

                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            selectedOrder = null
                        }
                    ) {
                        Text("Close")
                    }
                },
                dismissButton = {
                    if (order.status == "Completed") {
                        if (order.reviewRating != null) {
                            Text("Reviewed: ${order.reviewRating}/5")
                        } else {
                            TextButton(
                                onClick = {
                                    reviewOrder = order
                                    reviewRating = 0
                                    reviewComment = "" //显示评价状态
                                    reviewError = ""
                                    selectedOrder = null
                                }
                            ) {
                                Text("Write a Review")
                            }//订单详情按钮
                        }
                    }
                }
            )
        }

        reviewOrder?.let { order ->
            AlertDialog(
                onDismissRequest = {
                    if (!isSubmittingReview) {
                        reviewOrder = null
                    }
                },
                title = {
                    Text("Rate Your Order")
                },
                text = {
                    Column(
                        modifier = Modifier
                            .heightIn(max = 400.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text("Order ID: ${order.id}")

                        Spacer(modifier = Modifier.height(16.dp))

                        Text("How was your experience?")

                        Row {
                            (1..5).forEach { star ->
                                TextButton(
                                    onClick = {
                                        reviewRating = star
                                    }
                                ) {
                                    Text(
                                        if (star <= reviewRating) "★" else "☆",
                                        style = MaterialTheme.typography.headlineSmall
                                    )
                                }
                            }
                        }

                        Text(
                            if (reviewRating == 0)
                                "Select a rating"
                            else
                                "$reviewRating out of 5 stars"
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = reviewComment,
                            onValueChange = {
                                if (it.length <= 300) {
                                    reviewComment = it
                                }
                            },
                            label = {
                                Text("Write a review (optional)")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            maxLines = 5
                        )

                        Text("${reviewComment.length}/300")

                        Spacer(modifier = Modifier.height(12.dp))

                        if (reviewError.isNotEmpty()) {
                            Text(
                                text = reviewError,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        /*Text(
                        "Review saving will be added in the next step.",
                        style = MaterialTheme.typography.bodySmall
                    )*/
                    }
                },
                confirmButton = {
                    TextButton(
                        enabled = reviewRating in 1..5 && !isSubmittingReview,
                        onClick = {
                            if (
                                userId.isBlank() ||
                                order.status != "Completed" ||
                                order.reviewRating != null
                            ) {
                                reviewError = "This order cannot be reviewed."
                            } else {
                                isSubmittingReview = true
                                reviewError = ""

                                val reviewData = hashMapOf<String, Any>(
                                    "reviewRating" to reviewRating,
                                    "reviewComment" to reviewComment.trim(),
                                    "reviewedAt" to FieldValue.serverTimestamp()
                                )

                                db.collection("users")
                                    .document(userId)
                                    .collection("orders")
                                    .document(order.id)
                                    .update(reviewData)
                                    .addOnSuccessListener {
                                        isSubmittingReview = false
                                        reviewOrder = null
                                    }
                                    .addOnFailureListener { exception ->
                                        isSubmittingReview = false
                                        reviewError = "Review submission failed: " +
                                                (exception.localizedMessage
                                                    ?: "Please try again.")
                                    }
                            }
                        }
                    ) {
                        Text(
                            if (isSubmittingReview)
                                "Submitting..."
                            else
                                "Submit Review"
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        enabled = !isSubmittingReview,
                        onClick = {
                            reviewOrder = null
                        }
                    ) {
                        Text("Close")
                    }
                }
            )
        }
    }

    @Composable
    fun OrderStatusNotificationListener(
        db: FirebaseFirestore,
        userId: String
    ) {
        val context = LocalContext.current.applicationContext
        DisposableEffect(db, userId) {
            val previousStatuses = mutableMapOf<String, String>()
            var isFirstSnapshot = true
            val registration = db.collection("users")
                .document(userId)
                .collection("orders")
                .addSnapshotListener { snapshot, exception ->
                    if (exception != null) {
                        Log.e(
                            "SmartCafeNotifications",
                            "Order status listener failed",
                            exception
                        )
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val currentStatuses = snapshot.documents
                            .mapNotNull { document ->
                                document.getString("status")?.let { status ->
                                    document.id to status
                                }
                            }
                            .toMap()

                        if (!isFirstSnapshot) {
                            currentStatuses.forEach { (orderId, newStatus) ->
                                val oldStatus = previousStatuses[orderId]

                                if (
                                    oldStatus != null &&
                                    oldStatus != newStatus
                                ) {
                                    Log.d(
                                        "SmartCafeNotifications",
                                        "Order $orderId: $oldStatus -> $newStatus"
                                    )

                                    showOrderStatusNotification(
                                        context = context,
                                        orderId = orderId,
                                        status = newStatus
                                    )
                                }
                            }
                        }
                        previousStatuses.clear()
                        previousStatuses.putAll(currentStatuses)
                        isFirstSnapshot = false
                    }
                }
            onDispose {
                registration.remove()
            }
        }
    }

    @Suppress("DEPRECATION") //发送系统通知
    fun showOrderStatusNotification(
        context: Context,
        orderId: String,
        status: String
    ): Boolean {

        val channelId = "smart_cafe_order_updates"

        val manager = context.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager

        // Create a notification channel on Android 8.0+.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Order Updates",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications about order status changes"
            }

            manager.createNotificationChannel(channel)

            if (manager.getNotificationChannel(channelId)?.importance ==
                NotificationManager.IMPORTANCE_NONE
            ) {
                return false
            }
        }

        // Check Android 13+ notification permission.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (context.checkSelfPermission(
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }

        if (!manager.areNotificationsEnabled()) {
            return false
        }

        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(context, channelId)
        } else {
            Notification.Builder(context)
        }

        val notification = builder
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Smart Cafe · Order Update")
            .setContentText("Order ${orderId.take(8)} is now $status")
            .setAutoCancel(true)
            .build()

        return try {
            manager.notify(orderId.hashCode(), notification)
            true
        } catch (e: SecurityException) {
            false
        }
    }
}