**Smart Cafe PRD**

**PRD Version: v1.0
Date: 28 October 2026
Development Role: Customer**

1. Project Overview
  Smart Cafe is an Android mobile application for online coffee ordering. The app aims to help customers to browse the cafe menu, choose products, place orders and check their order status by using mobile phones.
  This project will use Kotlin as the main programming language, and Android Studio will be used for the development.

2. Problem & Value
  2.1 Problem
    Customers need an easy way to view the cafe menu, check the prices of products and finally complete their orders through the app.
    The app may also consider adding a popular products recommendation section, which can show products with higher sales to help customers make their choices.
  2.2 Value
    The app aims to provide a simple and clear mobile ordering process.
    Customers can:
      1.Browse the Smart Cafe menu.
      2.Choose products and add them into shopping cart.
      3.Submit orders and check order status.
      4.View their order history.

3. User Roles
	 3.1 Customer
   	 Customer is the main target user for this project.
   	 When using the app, customers need to:
   	   1.Register an account.
   	   2.Login to the app.
	   3.Browse the menu.
       4.Manage the shopping cart.
	   5.Submit orders.
	   6.View previous order records.
	 3.2 Admin
		 This project will mainly focus on the customer side of the application. Admin functions are not the main development priority.

4. Project Scope
   4.1 The app plans to include these functions:
   		1.User registration, login and logout.
   		2.Browse the menu and view product name, description and price.
   		3.Search or filter menu products.
   		4.Add products into the shopping cart.
   		5.Change the quantity of products or remove products from the shopping cart.
   		6.Confirm an order and receive order confirmation information.
   		7.View order history and current order status.
   4.2 Functions not included in the current project:
   		1.Customers can give ratings for products and services after the service is finished.
   		2.Customers can receive notifications when their order status is updated.
   		3.Show popular products based on sales data (this needs product sales or order data).
   	If development time is enough, these functions could be added in future versions.

5. Requirements
   5.1 Functional Requirements
   FR-01	Customers can register an account using email and password.(Must)
   FR-02	Customers can login and logout securely.(Must)
   FR-03	Customers can browse the menu and view product information, prices and availability status.(Must)
   FR-04	Customers can add products to shopping cart, change the quantity or remove products.(Must)
   FR-05	Customers can submit orders and receive order confirmation information.(Must)
   FR-06	Customers can view order history and current order status.(Must)
   FR-07	Customers can search or filter menu products.(Should)
   FR-08	Customers can submit ratings and text reviews.(Could)
		Priority Explanation:
		Must: Core functions that must be completed in this project.
   		Should: Important functions that are planned to be developed, but have lower priority than Must functions.
   		Could: Extra functions that may be developed if there is enough time.
   5.2 Non-functional Requirements
   		1.Security
   		2.Easy to use
   		3.Data completeness

6. Data Requirements
   The project plans to use the following main data entities.
   6.1 User
   		User ID - A unique ID to identify each user.
   		Email - The email address used for registration.
   		Authentication Information - Managed securely by the selected authentication service.
   6.2 MenuItem
   		Item ID - A unique ID to identify each product.
   		Item Name - The name of the product.
   		Item Description - Description of the product.
		Item Price - The price of the product.
		Availability Status - Shows if the product is available to purchase.
   6.3 Order
   		Order ID - A unique ID to identify each order.
  	  	Customer ID - Identifies the customer who placed the order.
		Order Items - Products included in the order and their quantities.
   		Total Price - The total price of the order.
   		Order Status - For example: Pending, Preparing, Ready for Pickup or Completed.
   		Order Time - Date and time when the order was created.
   The above is the initial design. The specific data fields and database structure will be decided in more detail during the development process.

7. Technical Constraints
   1.Kotlin must be used as the programming language.
   2.Android Studio must be used as the development environment.
   3.The user interface can be developed using Jetpack Compose or XML.
   4.The app needs to use suitable authentication and data persistence solutions.
   5.Firebase Authentication can be considered as a possible solution for user registration and login.
   6.Menu and order data can be saved using a suitable cloud database or local database.
   7.The final project must include at least one useful advanced technology integration, such as an external API, cloud service, notification or data analysis.
   Other specific technical solutions will be decided during the initial design and development stages.

8. Acceptance Criteria
	AC-01 — User Registration
		Precondition: The user has not registered an account before and provides valid registration information.
		Action: The user submits the registration form.
		Expected Results: 
			1.The system successfully creates a new user account.
			2.The app shows a registration success message.
			3.If the email has already been registered, the app should show an error message and prevent creating a duplicate account.
	AC-02: User Login
		Precondition: The user already has a registered account.
		Action: The user submits email and password.
		Expected Results: 
			1.If both email and password are correct, the user can login successfully and enter the customer menu page.
			2.If the email or password is incorrect, the app should display an error message.
	AC-03: Shopping Cart Management
		Precondition: There are available products in the menu.
		Action: The user adds products into shopping cart and changes the product quantity.
		Expected Results: 
			1.Products are correctly added into the shopping cart.
			2.Product quantity is updated when the user increase or decrease it.
			3.The total price of shopping cart is calculated correctly based on product prices and quantities.
			4.The user can remove products from the shopping cart.
	AC-04: Order Submission
		Precondition: The user's shopping cart contains valid products.
		Action: The user confirms and submits the order.
		Expected Results:
			 1.The system successfully creates a new order.
			 2.The app displays order confirmation information.
			 3.The order contains correct products, quantities and total price.
			 4.If the order submission fails, the app should display a clear error message.

9. Version History
v1.0   28 October 2026   Created the initial Product Requirements Document.




prd版本：v1.0
日期：2026年10月28日
开发角色：Customer（客户端）

1. 项目概述
   Smart Cafe app 是一款基于安卓移动平台的咖啡店线上点餐应用，旨在帮助顾客通过手机浏览菜单、选择商品、提交订单并查看订单状态
   本项目使用 Kotlin 作为主要编程语言，并通过 Android Studio 进行开发
2. 问题与价值(Problem & value)
   2-1.项目问题(Problem)
     顾客在使用应用时需要使用方便的方式来查看咖啡馆的菜单，了解商品价格，最后完成点餐
     考虑加入商品热门推荐展示销量较高的商品以辅助顾客进行点餐选择
   2-2.项目价值(value)
     app旨在提供简单、清晰的移动点餐流程，使顾客能够：
       1.浏览Smart Cafe菜单
       2.选择商品并加入购物车
       3.提交订单并查看订单状态
       4.查看历史订单
3. 用户角色(User roles)
   3-1.顾客(Customer)
     顾客是本项目的主要开发对象
     顾客在使用应用时需要：
        1.注册账户
        2.登录应用
        3.浏览菜单
        4.管理购物车
        5.提交订单
        6.查看历史订单记录
   3-2.店员(Admin)
     本项目以顾客端为主要开发方向，管理员功能不作为开发重点
4. 项目范围(Scope)
   4-1.应用计划包含以下功能:
     1.用户注册、登录和退出登录
     2.浏览菜单并查看商品名称、描述、价格
     3.搜索或筛选菜单商品
     4.将商品加入购物车
     5.修改购物车中的商品数量或移除商品
     6.确认订单并获得订单确认信息
     7.查看历史订单和当前订单状态
   4-2.此项目暂时不包括的功能：
     1.顾客可在服务结束后对商品和服务进行打分
     2.顾客可在订单状态更新时获取通知
     3.根据商品销量向顾客展示销量较高的商品（需要商品销量或订单数据）
   如果开发时间允许，可以在后续版本中增加这些功能。
5. 需求
   5-1.功能需求(Requirements)
    编号	           功能需求	                        优先级
   FR-01	顾客可以使用邮箱和密码注册账户	                 Must
   FR-02	顾客可以安全地登录和退出登录	                   Must
   FR-03	顾客可以浏览菜单并查看商品信息、价格和供应状态   Must
   FR-04	顾客可以将商品加入购物车，并修改数量或移除商品   Must
   FR-05	顾客可以提交订单并获得订单确认信息              Must
   FR-06	顾客可以查看历史订单和当前订单状态              Must
   FR-07	顾客可以搜索或筛选菜单商品                     Should
   FR-08	顾客可以提交评分和文字评价                     Could
   优先级说明：
    Must： 项目必须实现的核心功能
    Should： 重要功能，计划实现，但优先级低于核心功能
    Could： 有额外时间时可以考虑实现的功能
   5-2.非功能需求
     1.安全性
     2.易用性
     3.数据完整
6.数据需求(Data)
  项目计划使用以下主要数据实体
  6-1.用户（User）
     字段	               说明
   用户 ID	          唯一标识用户	
   邮箱	              用户的注册邮箱	
   身份验证信息	      由选定的身份验证服务安全管理
  6-2.菜单商品（MenuItem）
   字段	               说明
  商品 ID              唯一标识商品
  商品名称              商品的名称
  商品描述              商品的介绍
  商品价格              商品的价格
  供应状态              标记商品是否可购买
  6-3.订单（Order）
   字段	                 说明
  订单ID                唯一标识订单
  顾客ID                标识下单用户
  订单商品               订单中包含的商品及其数量
  订单总价               订单的总金额
  订单状态               例如待处理、准备中、可取餐或已完成
  下单时间               订单创建的日期和时间
以上为初步设计，具体字段和数据库结构将在开发过程中进一步确定
7. 技术约束(Constraints)
   1.必须使用 Kotlin 作为开发语言
   2.必须使用 Android Studio 作为开发环境
   3.用户界面可以使用 Jetpack Compose 或 XML
   4.应用需要使用合适的身份验证方案和数据持久化方案
   5.Firebase Authentication 可以作为用户注册和登录的候选方案
   6.菜单和订单数据可以使用合适的云端数据库或本地数据库保存
   7.最终项目必须实现至少一项有实际用途的高级技术集成，例如外部 API、云服务、通知或数据分析
其他具体技术方案将在初步设计和开发阶段确定
8. 验收标准(Acceptance criteria)
   AC-01：用户注册：
    前提：用户尚未注册，并且提供了有效的注册信息
	操作：用户提交注册表单
    预期结果：
   		系统成功创建用户账户
   		应用显示注册成功提示
   		如果邮箱已经注册过，应用应显示错误信息，并阻止重复账户的创建
   AC-02：用户登录
   	前提：用户已经注册
    操作：用户提交邮箱和密码
    预期结果：
   		如果邮箱和密码皆正确，则用户成功登录并进入顾客菜单页面
   		如果邮箱和密码错误，应用应提示错误信息
   AC-03：购物车管理
   	前提：菜单中存在可购买的商品
   	操作：用户将商品加入购物车，并修改商品数量
    预期结果：
    	商品被正确添加进购物车
   		商品数量根据用户的增加和减少更新
   		购物车总价根据商品价格和数量正确计算
   		用户可以移除购物车中的商品
	AC-04：提交订单
      前提：用户的购物车中存在有效商品
	  操作： 用户确认并提交订单 
	  预期结果：
		系统成功创建订单 
   		应用显示订单确认信息
   		订单包含正确的商品、数量和总价
   		如果订单提交失败，应用显示明确的错误信息  
9. 版本记录
V1.0	  2026年10月28日	   创建初始产品需求文档







