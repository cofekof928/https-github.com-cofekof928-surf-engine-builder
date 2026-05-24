package com.example.ui

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.BookmarkItem
import com.example.data.HistoryItem
import com.example.data.SavedResearch
import java.text.SimpleDateFormat
import java.util.*

// Advanced Futuristic Dark Palette
val DarkBg = Color(0xFF0D0D0E)
val SolidSurface = Color(0xFF151518)
val LightSurface = Color(0xFF1F1F24)
val AccentPurple = Color(0xFFBB86FC)
val AccentTeal = Color(0xFF4DD0E1)
val AccentGold = Color(0xFFFFD54F)
val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFF8F8F99)
val SecurityGreen = Color(0xFF00E676)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResearchBrowserApp(viewModel: BrowserViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    
    // Bookmark status of current active URL check
    var isCurrentBookmarked by remember { mutableStateOf(false) }
    LaunchedEffect(uiState.activeUrl, uiState.bookmarksList) {
        isCurrentBookmarked = viewModel.isCurrentUrlBookmarked(uiState.activeUrl)
    }

    // Dynamic withdrawal message notifications
    LaunchedEffect(uiState.withdrawSuccessMessage, uiState.withdrawErrorMessage) {
        if (uiState.withdrawSuccessMessage != null) {
            Toast.makeText(context, uiState.withdrawSuccessMessage, Toast.LENGTH_LONG).show()
            viewModel.clearNotifications()
        }
        if (uiState.withdrawErrorMessage != null) {
            Toast.makeText(context, uiState.withdrawErrorMessage, Toast.LENGTH_LONG).show()
            viewModel.clearNotifications()
        }
    }

    // Back handling for WebView
    BackHandler(enabled = uiState.currentTab == BrowserTab.BROWSER && uiState.canGoBack) {
        webViewInstance?.goBack()
    }

    Scaffold(
        bottomBar = {
            BrowserBottomBar(
                currentTab = uiState.currentTab,
                onTabSelected = { selectedTab ->
                    viewModel.setTab(selectedTab)
                },
                historyCount = uiState.historyList.size
            )
        },
        containerColor = DarkBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(DarkBg)
        ) {
            // Elegant Status / Quick Control Info
            SystemStatusHeader()

            // Main View Selector with clean transitions
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when (uiState.currentTab) {
                    BrowserTab.BROWSER -> WebTabHome(
                        viewModel = viewModel,
                        uiState = uiState,
                        isBookmarked = isCurrentBookmarked,
                        webViewInstance = webViewInstance,
                        onWebViewCreated = { webView ->
                            webViewInstance = webView
                        }
                    )
                    BrowserTab.WALLET_REWARDS -> WalletAndRewardsTab(
                        viewModel = viewModel,
                        uiState = uiState
                    )
                    BrowserTab.DOWNLOADS -> DownloadsTab(
                        viewModel = viewModel,
                        uiState = uiState
                    )
                    BrowserTab.TAKEOUT_CLOUD -> TakeoutCloudTab(
                        viewModel = viewModel,
                        uiState = uiState
                    )
                    BrowserTab.TOR_SECURITY -> TorSecurityTab(
                        viewModel = viewModel,
                        uiState = uiState
                    )
                    BrowserTab.SAVED_RESEARCH -> SavedResearchWorkspace(
                        viewModel = viewModel,
                        uiState = uiState
                    )
                }
            }
        }
    }
}

@Composable
fun SystemStatusHeader() {
    val sdf = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    var currentTime by remember { mutableStateOf(sdf.format(Date())) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = sdf.format(Date())
            kotlinx.coroutines.delay(15000)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkBg)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(SecurityGreen, RoundedCornerShape(50))
            )
            Text(
                text = "TOR مشفر",
                color = SecurityGreen,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        Text(
            text = currentTime,
            color = TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.VpnLock,
                contentDescription = "Vpn",
                tint = AccentPurple,
                modifier = Modifier.size(13.dp)
            )
            Icon(
                imageVector = Icons.Default.WifiLock,
                contentDescription = "Encrypted Wifi",
                tint = AccentTeal,
                modifier = Modifier.size(13.dp)
            )
        }
    }
}

/**
 * Tab 1: Private Web Browser Tab with DuckDuckGo Style Layout & Rectangular Address Bar
 */
@Composable
fun WebTabHome(
    viewModel: BrowserViewModel,
    uiState: BrowserUiState,
    isBookmarked: Boolean,
    webViewInstance: WebView?,
    onWebViewCreated: (WebView) -> Unit
) {
    var isPrivacyPopupVisible by remember { mutableStateOf(false) }
    var localAddressInput by remember { mutableStateOf(uiState.searchInput) }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Sync input when active page shifts
    LaunchedEffect(uiState.activeUrl) {
        localAddressInput = uiState.activeUrl
    }

    Column(modifier = Modifier.fillMaxSize()) {
        
        // Horizontal Web Tabs Controller Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SolidSurface)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LazyRow(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(uiState.tabs) { tab ->
                    val isActive = tab.id == uiState.activeTabId
                    Row(
                        modifier = Modifier
                            .background(
                                if (isActive) AccentPurple.copy(alpha = 0.25f) else LightSurface,
                                RoundedCornerShape(4.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = if (isActive) AccentPurple else Color.Transparent,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .clickable { viewModel.selectTab(tab.id) }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = if (tab.title.contains("duckduckgo")) "الرئيسية Research" else tab.title,
                            color = if (isActive) AccentPurple else TextPrimary,
                            fontSize = 11.sp,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.widthIn(max = 110.dp)
                        )
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clickable { viewModel.closeTab(tab.id) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close tab",
                                tint = if (isActive) AccentPurple else TextSecondary,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }

            // Quick add new tab button
            IconButton(
                onClick = { viewModel.createNewTab() },
                modifier = Modifier
                    .size(28.dp)
                    .background(LightSurface, RoundedCornerShape(4.dp))
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New Tab",
                    tint = SecurityGreen,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        
        // DuckDuckGo-style Rectangular Customizable Toolbar Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SolidSurface)
                .padding(horizontal = uiState.addressBarHorizontalPaddingDp.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // BACK BUTTON
            IconButton(
                onClick = { webViewInstance?.goBack() },
                enabled = uiState.canGoBack,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = if (uiState.canGoBack) TextPrimary else TextSecondary.copy(alpha = 0.4f),
                    modifier = Modifier.size(20.dp)
                )
            }

            // FORWARD BUTTON
            IconButton(
                onClick = { webViewInstance?.goForward() },
                enabled = uiState.canGoForward,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Forward",
                    tint = if (uiState.canGoForward) TextPrimary else TextSecondary.copy(alpha = 0.4f),
                    modifier = Modifier.size(20.dp)
                )
            }

            // DuckDuckGo Shield/Lock security icon
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(LightSurface, RoundedCornerShape(uiState.addressBarCornerRadiusDp.dp))
                    .clickable { isPrivacyPopupVisible = true }
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (uiState.isTorEnabled) Icons.Default.Shield else Icons.Default.Lock,
                    contentDescription = "Privacy Shield",
                    tint = if (uiState.isTorEnabled) SecurityGreen else AccentPurple,
                    modifier = Modifier.size(18.dp)
                )
            }

            // RECTANGULAR CUSTOMIZABLE ADDRESS INPUT BAR
            TextField(
                value = localAddressInput,
                onValueChange = { localAddressInput = it },
                modifier = Modifier
                    .weight(1f)
                    .height(uiState.addressBarHeightDp.dp)
                    .border(
                        width = 1.dp,
                        color = if (uiState.isTorEnabled) SecurityGreen.copy(alpha = 0.4f) else AccentPurple.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(uiState.addressBarCornerRadiusDp.dp)
                    ),
                shape = RoundedCornerShape(uiState.addressBarCornerRadiusDp.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = LightSurface,
                    unfocusedContainerColor = LightSurface,
                    disabledContainerColor = LightSurface,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search,
                    keyboardType = KeyboardType.Uri
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        keyboardController?.hide()
                        viewModel.updateSearchInput(localAddressInput)
                        viewModel.navigateToInputUrl(localAddressInput)
                    }
                ),
                trailingIcon = {
                    if (localAddressInput.isNotBlank()) {
                        IconButton(onClick = { localAddressInput = "" }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            )

            // Bookmark Toggle Inside Header
            IconButton(
                onClick = {
                    viewModel.toggleBookmark(uiState.webTitle, uiState.activeUrl)
                },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    contentDescription = "Bookmark",
                    tint = if (isBookmarked) AccentGold else TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Privacy info popup dropdown
        if (isPrivacyPopupVisible) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SolidSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .border(1.dp, SecurityGreen, RoundedCornerShape(8.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🕵️ نظام بحث Research المشفّر بالكامل",
                            color = SecurityGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        IconButton(
                            onClick = { isPrivacyPopupVisible = false },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "محرك البحث والناقل المدمج محمي تماماً عبر بروتوكول التشفير العسكري AES-256 ونظام Onion. لا يتم جمع أي ملفات تعريف ارتباط للخصوصية التامة.",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• معرّف الأمان: SHA-512-AES-GCM-SECURE\n• قنوات العبور النشطة: 3 نقاط تور مشفرة\n• التصفح الحالي: خاص ومخفي تماماً",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }
            }
        }

        // Main Web View Canvas
        Box(modifier = Modifier.weight(1f)) {
            WebBrowserView(
                activeUrl = uiState.activeUrl,
                onPageStarted = { url ->
                    // Set loading state if needed
                },
                onPageFinished = { url, title ->
                    viewModel.onWebPageVisited(title, url)
                    viewModel.setWebNavigationProperties(
                        canBack = webViewInstance?.canGoBack() ?: false,
                        canForward = webViewInstance?.canGoForward() ?: false
                    )
                },
                onWebViewCreated = onWebViewCreated
            )

            // Research-like privacy overlay when on initial search page to prompt actions
            if (uiState.activeUrl == "https://duckduckgo.com" || uiState.activeUrl == "https://duckduckgo.com/") {
                ResearchStartCanvas(viewModel)
            }
        }
    }
}

@Composable
fun ResearchStartCanvas(viewModel: BrowserViewModel) {
    var searchInputLocal by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg.copy(alpha = 0.95f))
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .background(
                                Brush.linearGradient(listOf(SecurityGreen, AccentTeal)),
                                RoundedCornerShape(18.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TravelExplore,
                            contentDescription = "Research Secure Browser",
                            tint = DarkBg,
                            modifier = Modifier.size(42.dp)
                        )
                    }
                    Text(
                        text = "RESEARCH PRIVATE",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "محرك بحث مدمج مشفّر • تصفح علمي بحرية مطلقة",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SolidSurface),
                    shape = RoundedCornerShape(0.dp), // Rectangular!
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, AccentTeal.copy(alpha = 0.5f), RoundedCornerShape(0.dp))
                        .padding(2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = searchInputLocal,
                            onValueChange = { searchInputLocal = it },
                            placeholder = {
                                Text(
                                    "ابحث بالويب أو اطلب بحثاً علمياً مفصلاً بالـ AI...",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SecurityGreen,
                                unfocusedBorderColor = LightSurface,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search icon",
                                    tint = SecurityGreen
                                )
                            },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = {
                                keyboardController?.hide()
                                if (searchInputLocal.isNotBlank()) {
                                    viewModel.navigateToInputUrl(searchInputLocal)
                                }
                            })
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    keyboardController?.hide()
                                    if (searchInputLocal.isNotBlank()) {
                                        viewModel.navigateToInputUrl(searchInputLocal)
                                    } else {
                                        viewModel.navigateToInputUrl("https://duckduckgo.com")
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = LightSurface),
                                shape = RoundedCornerShape(0.dp) // Square design
                            ) {
                                Icon(Icons.Default.Language, contentDescription = "Browse", tint = AccentTeal, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("تصفح الويب", fontSize = 11.sp, color = TextPrimary)
                            }

                            Button(
                                onClick = {
                                    keyboardController?.hide()
                                    if (searchInputLocal.isNotBlank()) {
                                        viewModel.performAiResearch(searchInputLocal)
                                    }
                                },
                                modifier = Modifier.weight(1.1f),
                                colors = ButtonDefaults.buttonColors(containerColor = SecurityGreen),
                                shape = RoundedCornerShape(0.dp)
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = "AI Action", tint = DarkBg, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("بحوث ذكية AI", fontSize = 11.sp, color = DarkBg, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    QuickBadgeLink("جوجل سکولار", "https://scholar.google.com") { viewModel.navigateToInputUrl(it) }
                    QuickBadgeLink("ويكيبيديا", "https://ar.wikipedia.org") { viewModel.navigateToInputUrl(it) }
                    QuickBadgeLink("أرشيف البحث", "https://archive.org") { viewModel.navigateToInputUrl(it) }
                }
            }
        }
    }
}

@Composable
fun QuickBadgeLink(label: String, url: String, onClick: (String) -> Unit) {
    Box(
        modifier = Modifier
            .background(LightSurface, RoundedCornerShape(4.dp))
            .clickable { onClick(url) }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(Modifier.size(6.dp).background(AccentTeal, RoundedCornerShape(50)))
            Text(label, color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        }
    }
}

/**
 * Tab 2: Electronic Wallet with Cash-outs (InstaPay, Orange Cash, Crypto) & Referral Program
 */
@Composable
fun WalletAndRewardsTab(viewModel: BrowserViewModel, uiState: BrowserUiState) {
    var activeSubTab by remember { mutableStateOf(0) } // 0: Wallet Cash-Out, 1: Referrals, 2: Reward tasks
    
    // InstaPay State
    var instapayIpn by remember { mutableStateOf("") }
    var instapayAmount by remember { mutableStateOf("") }

    // Orange Cash State
    var orangePhone by remember { mutableStateOf("") }
    var orangeAmount by remember { mutableStateOf("") }

    // Crypto State
    var cryptoAddress by remember { mutableStateOf("") }
    var cryptoAmountSat by remember { mutableStateOf("") }
    var selectedCryptoCurrency by remember { mutableStateOf("BTC") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Quick Total Balance Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SolidSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, AccordionGoldBorderColor().copy(alpha = 0.3f), RoundedCornerShape(0.dp)),
                shape = RoundedCornerShape(0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("محفظة البحث الآمن الإلكترونية M-Wallet 💸", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "${String.format(Locale.US, "%,.2f", uiState.walletBalanceEgp)} EGP", color = AccentPurple, fontSize = 26.sp, fontWeight = FontWeight.Black)
                            Text(text = "كاش الجنيه المصري", color = TextSecondary, fontSize = 11.sp)
                        }
                        Divider(modifier = Modifier.height(40.dp).width(1.dp), color = LightSurface)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "%,d".format(Locale.US, uiState.walletBalanceSatoshis), color = AccentTeal, fontSize = 26.sp, fontWeight = FontWeight.Black)
                            Text(text = "رصيد ساتوشي العملات الرقمية", color = TextSecondary, fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Daily Claim Button
                        Button(
                            onClick = { viewModel.claimDailyBonus() },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (uiState.dailyBonusClaimedToday) LightSurface else AccentPurple
                            ),
                            enabled = !uiState.dailyBonusClaimedToday,
                            shape = RoundedCornerShape(0.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CardGiftcard,
                                contentDescription = "Daily Rewards",
                                tint = if (uiState.dailyBonusClaimedToday) TextSecondary else DarkBg,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (uiState.dailyBonusClaimedToday) "تم جمع هدية اليوم ✔" else "احصل على الهدية اليومية",
                                fontSize = 11.sp,
                                color = if (uiState.dailyBonusClaimedToday) TextSecondary else DarkBg,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Sub Tabs switcher
        item {
            ScrollableTabRow(
                selectedTabIndex = activeSubTab,
                containerColor = DarkBg,
                edgePadding = 0.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[activeSubTab]),
                        color = AccentPurple
                    )
                }
            ) {
                Tab(
                    selected = activeSubTab == 0,
                    onClick = { activeSubTab = 0 },
                    text = { Text("سحب كاش فوري", color = if (activeSubTab == 0) AccentPurple else TextSecondary, fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
                Tab(
                    selected = activeSubTab == 1,
                    onClick = { activeSubTab = 1 },
                    text = { Text("نظام الإحالة والعمولة", color = if (activeSubTab == 1) AccentPurple else TextSecondary, fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
                Tab(
                    selected = activeSubTab == 2,
                    onClick = { activeSubTab = 2 },
                    text = { Text("سجل المعاملات", color = if (activeSubTab == 2) AccentPurple else TextSecondary, fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
            }
        }

        if (activeSubTab == 0) {
            // Settle Instant cashout panels
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SolidSurface),
                    shape = RoundedCornerShape(0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "📤 سحب فوري كاش عبر انستاباي INSTAPAY",
                            color = AccentTeal,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "اسحب رصيدك كاش من التطبيق إلى حسابك البنكي أو رقم انستاباي مباشرة وسرية تامة.",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                        OutlinedTextField(
                            value = instapayIpn,
                            onValueChange = { instapayIpn = it },
                            label = { Text("عنوان InstaPay (مثال: handle@instapay)", fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentTeal,
                                focusedLabelColor = AccentTeal,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                        OutlinedTextField(
                            value = instapayAmount,
                            onValueChange = { instapayAmount = it },
                            label = { Text("المبلغ المراد سحبه بالجنيه (EGP)", fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentTeal,
                                focusedLabelColor = AccentTeal,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                        Button(
                            onClick = {
                                val amt = instapayAmount.toDoubleOrNull() ?: 0.0
                                if (viewModel.withdrawInstapay(instapayIpn, amt)) {
                                    instapayAmount = ""
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentTeal),
                            shape = RoundedCornerShape(0.dp)
                        ) {
                            Text("تأكيد السحب كاش على انستاباي 📲", color = DarkBg, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SolidSurface),
                    shape = RoundedCornerShape(0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "🍊 سحب كاش محفظة أورانج كاش Orange Cash",
                            color = Color(0xFFFF6600),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "قم بتحويل رصيدك الحالي فورياً إلى محفظة فودافون أو أورنج أو اتصالات كاش بسهولة.",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                        OutlinedTextField(
                            value = orangePhone,
                            onValueChange = { orangePhone = it },
                            label = { Text("رقم هاتف أورنج كاش (11 رقم)", fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFF6600),
                                focusedLabelColor = Color(0xFFFF6600),
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                        OutlinedTextField(
                            value = orangeAmount,
                            onValueChange = { orangeAmount = it },
                            label = { Text("المبلغ المطلوب بالجنيه المصري (EGP)", fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFF6600),
                                focusedLabelColor = Color(0xFFFF6600),
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                        Button(
                            onClick = {
                                val amt = orangeAmount.toDoubleOrNull() ?: 0.0
                                if (viewModel.withdrawOrangeCash(orangePhone, amt)) {
                                    orangeAmount = ""
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6600)),
                            shape = RoundedCornerShape(0.dp)
                        ) {
                            Text("تأكيد السحب كاش على أورانج كاش 🍊", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SolidSurface),
                    shape = RoundedCornerShape(0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "🪙 سحب للعملات الرقمية المشفرة CRYPTO (BTC / USDT)",
                            color = AccentPurple,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf("BTC", "USDT").forEach { coin ->
                                FilterChip(
                                    selected = selectedCryptoCurrency == coin,
                                    onClick = { selectedCryptoCurrency = coin },
                                    label = { Text(coin) },
                                    shape = RoundedCornerShape(0.dp)
                                )
                            }
                        }
                        OutlinedTextField(
                            value = cryptoAddress,
                            onValueChange = { cryptoAddress = it },
                            label = { Text("أدخل عنوان محفظة العملات الرقمية المستلمة", fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentPurple,
                                focusedLabelColor = AccentPurple,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                        OutlinedTextField(
                            value = cryptoAmountSat,
                            onValueChange = { cryptoAmountSat = it },
                            label = { Text("المبلغ المطلوب بالساتوشي / السنت الرقمي", fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentPurple,
                                focusedLabelColor = AccentPurple,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                        Button(
                            onClick = {
                                val sat = cryptoAmountSat.toLongOrNull() ?: 0L
                                if (viewModel.withdrawCrypto(cryptoAddress, sat, selectedCryptoCurrency)) {
                                    cryptoAmountSat = ""
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                            shape = RoundedCornerShape(0.dp)
                        ) {
                            Text("تحويل مشفر فوري على البلوكشين ₿", color = DarkBg, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        if (activeSubTab == 1) {
            // Referral view
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SolidSurface),
                    shape = RoundedCornerShape(0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("👥 نظام الإحالة لكسب العمولات والجوائز المادية", color = AccentPurple, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(
                            text = "قم بنشر كود الإحالة الخاص بك أو الرابط بين أصدقائك وطلبة البحث العلمي وكلياتك، وستكسب عمولة مادية تبلغ 30 EGP لكل عملية تفعيل أو بحث علمي ذكي يقومون بها!",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )

                        Divider(color = LightSurface)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("كود الإحالة المشفر الفريد:", color = TextSecondary, fontSize = 11.sp)
                                Text(uiState.referralCode, color = SecurityGreen, fontSize = 18.sp, fontWeight = FontWeight.Black)
                            }

                            val clipboard = LocalClipboardManager.current
                            val contextLocal = LocalContext.current
                            Button(
                                onClick = {
                                    clipboard.setText(AnnotatedString("حمل متصفح الأبحاث المشفر Research واستخدم كود إحالتي ${uiState.referralCode} واكسب 50 EGP فوراً! https://research-onion-browser.net"))
                                    Toast.makeText(contextLocal, "تم نسخ رابط الإحالة والرسالة!", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(0.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = LightSurface)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = AccentTeal, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("نسخ الدعوة", fontSize = 11.sp, color = AccentTeal)
                            }
                        }

                        Divider(color = LightSurface)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${uiState.invitedTotal} طلاب", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                Text("إجمالي الأصدقاء", color = TextSecondary, fontSize = 10.sp)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("15%", color = AccentTeal, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                Text("معدل العمولة كاش", color = TextSecondary, fontSize = 10.sp)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${uiState.referralEarningsEgp} EGP", color = AccentGold, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                Text("إجمالي أرباحك", color = TextSecondary, fontSize = 10.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = { viewModel.generateSimulatedInvite() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = LightSurface),
                            border = PaddingValues(0.dp).let { BorderStroke(1.dp, AccentPurple.copy(alpha = 0.5f)) },
                            shape = RoundedCornerShape(0.dp)
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = "Add friend", tint = AccentPurple, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("محاكاة انضمام صديق (+30 EGP عمولة) 👥", fontSize = 11.sp, color = AccentPurple)
                        }
                    }
                }
            }
        }

        if (activeSubTab == 2) {
            // list transactions
            if (uiState.transactionsList.isEmpty()) {
                item {
                    ListEmptyState(title = "سجل العمليات فارغ", hint = "المعاملات والتسديدات والدفعات يتم تسجيلها مشفرة بشكل فوري.")
                }
            } else {
                items(uiState.transactionsList) { tx ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SolidSurface),
                        shape = RoundedCornerShape(0.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(BorderStroke(1.dp, LightSurface), RoundedCornerShape(0.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(tx.type, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("الجهة: ${tx.destination} • ${tx.date}", color = TextSecondary, fontSize = 11.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(tx.amount, color = if (tx.amount.startsWith("+")) SecurityGreen else Color.Red, fontWeight = FontWeight.Black, fontSize = 14.sp)
                                Box(
                                    modifier = Modifier
                                        .background(SecurityGreen.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(tx.status, color = SecurityGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Tab 3: Downloads Library - Tracks simulated downloaded academic files/PDF studies
 */
@Composable
fun DownloadsTab(viewModel: BrowserViewModel, uiState: BrowserUiState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "📚 مكتبة التنزيلات الآمنة",
                color = AccentPurple,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${uiState.downloadsList.size} ملفات منزلة",
                color = TextSecondary,
                fontSize = 11.sp
            )
        }
        
        Text(
            text = "تصفح أوراقك البحثية والكتب الأكاديمية والملفات التي قمت بتثبيتها وتنزيلها مشفرة محلياً ودون الكشف عن هويتك.",
            color = TextSecondary,
            fontSize = 11.sp,
            lineHeight = 16.sp
        )

        Divider(color = LightSurface)

        if (uiState.downloadsList.isEmpty()) {
            ListEmptyState(title = "مكتبة التنزيلات فارغة", hint = "تصفح الويب وعند الدخول أو الضغط على رابط لملف PDF علمي أو دراسة، سيتم تنزيله تلقائياً وعرضه هنا.")
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(uiState.downloadsList) { file ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SolidSurface),
                        shape = RoundedCornerShape(0.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(file.fileName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text("الحجم: ${file.sizeMb} MB • المصدر: ${file.urlSource}", color = TextSecondary, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.padding(top = 4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(SecurityGreen, RoundedCornerShape(50))
                                    )
                                    Text("تم التنزيل والتحقق من التشفير ✔", color = SecurityGreen, fontSize = 10.sp)
                                }
                            }
                            IconButton(onClick = { viewModel.removeDownload(file.id) }) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.8f))
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Tab 4: Takeout Cloud Account & Secure Encryption Mappings with Interactive Login
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TakeoutCloudTab(viewModel: BrowserViewModel, uiState: BrowserUiState) {
    var isLoginMode by remember { mutableStateOf(true) }
    var emailInput by remember { mutableStateOf("") }
    var usernameInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Safe check: If not logged in, prompt user with login or registration interface
        if (!uiState.isLoggedIn) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SolidSurface),
                    shape = RoundedCornerShape(0.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(BorderStroke(1.dp, AccentPurple), RoundedCornerShape(0.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Title Header
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "👤 حساب متصفح البحث Research Account",
                                color = AccentPurple,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }

                        Text(
                            text = "سجل دخولك أو أنشئ حساباً جديداً للوصول إلى نظام المزامنة السحابية المشفرة وحماية رصيد مكافآتك والوصول لأبحاثك من أي جهاز.",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )

                        // Mode Selector Tab (Login / Register)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(LightSurface, RoundedCornerShape(4.dp))
                                .padding(4.dp)
                        ) {
                            Button(
                                onClick = { 
                                    isLoginMode = true 
                                    viewModel.clearLoginError()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isLoginMode) AccentPurple else Color.Transparent,
                                    contentColor = if (isLoginMode) DarkBg else TextPrimary
                                ),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                Text("تسجيل الدخول", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { 
                                    isLoginMode = false 
                                    viewModel.clearLoginError()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (!isLoginMode) AccentPurple else Color.Transparent,
                                    contentColor = if (!isLoginMode) DarkBg else TextPrimary
                                ),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                Text("إنشاء حساب", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Input fields
                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = { emailInput = it },
                            label = { Text("البريد الإلكتروني", color = TextSecondary) },
                            textStyle = LocalTextStyle.current.copy(color = TextPrimary),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = AccentPurple) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentPurple,
                                unfocusedBorderColor = LightSurface,
                                focusedLabelColor = AccentPurple
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (!isLoginMode) {
                            OutlinedTextField(
                                value = usernameInput,
                                onValueChange = { usernameInput = it },
                                label = { Text("اسم المستخدم كامل", color = TextSecondary) },
                                textStyle = LocalTextStyle.current.copy(color = TextPrimary),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = AccentPurple) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AccentPurple,
                                    unfocusedBorderColor = LightSurface,
                                    focusedLabelColor = AccentPurple
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = { passwordInput = it },
                            label = { Text("كلمة المرور المشفرة", color = TextSecondary) },
                            textStyle = LocalTextStyle.current.copy(color = TextPrimary),
                            singleLine = true,
                            visualTransformation = if (showPassword) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = AccentPurple) },
                            trailingIcon = {
                                IconButton(onClick = { showPassword = !showPassword }) {
                                    Icon(
                                        imageVector = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Show password",
                                        tint = TextSecondary
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentPurple,
                                unfocusedBorderColor = LightSurface,
                                focusedLabelColor = AccentPurple
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Error message
                        uiState.loginErrorMessage?.let { errMsg ->
                            Text(
                                text = "⚠️ $errMsg",
                                color = Color.Red,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Submit Button
                        Button(
                            onClick = {
                                if (isLoginMode) {
                                    val success = viewModel.loginUser(emailInput, passwordInput)
                                    if (success) {
                                        emailInput = ""
                                        passwordInput = ""
                                    }
                                } else {
                                    val success = viewModel.registerUser(emailInput, usernameInput, passwordInput)
                                    if (success) {
                                        emailInput = ""
                                        usernameInput = ""
                                        passwordInput = ""
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = if (isLoginMode) "تسجيل الدخول الآمن" else "إنشاء الحساب ومزامنته",
                                color = DarkBg,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        Text(
                            text = "🔒 يتم حفظ كلمات المرور والبيانات محلياً بتشفير عسكري من خلال الـ KeyStore ولا يتم تخزينها أبداً بشكل مكشوف.",
                            color = SecurityGreen,
                            fontSize = 9.sp,
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        } else {
            // User is logged in! Show profile section card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SolidSurface),
                    shape = RoundedCornerShape(0.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(BorderStroke(1.dp, SecurityGreen), RoundedCornerShape(0.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(AccentPurple.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                                    .border(1.dp, AccentPurple, RoundedCornerShape(20.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = (uiState.loggedInUsername ?: "R").take(1).uppercase(),
                                    color = AccentPurple,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }
                            Column {
                                Text(
                                    text = uiState.loggedInUsername ?: "مستخدم Research",
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = uiState.loggedInEmail ?: "",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                        
                        Button(
                            onClick = { viewModel.logoutUser() },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.Red
                            ),
                            border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(4.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("تسجيل الخروج", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // System status and Sync Options Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SolidSurface),
                shape = RoundedCornerShape(0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(1.dp, AccentTeal), RoundedCornerShape(0.dp))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "☁️ نظام السحابة Takeout Cloud Account",
                            color = AccentTeal,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Box(
                            modifier = Modifier
                                .background(if (uiState.isCloudSynced) SecurityGreen.copy(alpha = 0.15f) else Color.Red.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (uiState.isCloudSynced) "مزامنة مشفرة ونشطة" else "غير مزامن بالأعلى",
                                color = if (uiState.isCloudSynced) SecurityGreen else Color.Red,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(
                        text = "سحابة Takeout مشفرة تماماً بتقنية التشفير من طرف إلى طرف (End-to-End Encryption AES-256). عند المزامنة، يتم تشفير كافة الإشارات المرجعية، سجل الزيارات، المحفظة والعمليات، والبحوث العلمية التي تجريها بمفتاح الأمان الخاص بك سرياً، ورفعها على خادم Takeout السحابي اللامركزي بأمان تام.",
                        color = TextPrimary,
                        fontSize = 11.sp,
                        lineHeight = 17.sp
                    )

                    Divider(color = LightSurface)

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("• مفتاح الأمان السحابي (AES-256 Seed Key):", color = TextSecondary, fontSize = 10.sp)
                        Text(
                            text = uiState.cloudEncryptedBackupKey,
                            color = AccentPurple,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("حالة المزامنة السحابية:", color = TextSecondary, fontSize = 11.sp)
                        Text(uiState.lastSyncTime, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Upload and Sync Everything Controls Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SolidSurface),
                shape = RoundedCornerShape(0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "📦 العناصر الجاهزة للرفع والتشفير اللامركزي:",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )

                    // Detail item tallies
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("📍 العلامات المرجعية والروابط", color = TextSecondary, fontSize = 11.sp)
                        Text("${uiState.bookmarksList.size} روابط", color = AccentPurple, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("🕒 سجل الزيارات المنظَّف", color = TextSecondary, fontSize = 11.sp)
                        Text("${uiState.historyList.size} زيارات", color = AccentPurple, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("🧠 أبحاث AI المحفوظة", color = TextSecondary, fontSize = 11.sp)
                        Text("${uiState.savedResearchList.size} دراسات", color = AccentPurple, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("💾 ملفات منزلة ومكتبة التنزيلات", color = TextSecondary, fontSize = 11.sp)
                        Text("${uiState.downloadsList.size} ملفات", color = AccentPurple, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    if (uiState.isSyncingNow) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(color = AccentTeal, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("جاري ضغط وتشفير ورفع الملفات بأمان AES-256...", color = AccentTeal, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    } else {
                        Button(
                            onClick = { viewModel.performTakeoutCloudSync() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentTeal),
                            shape = RoundedCornerShape(0.dp)
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = "Sync", tint = DarkBg, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("مزامنة مشفرة لكل شيء ورفع للأعلى 🔒", color = DarkBg, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Tab 5: TOR onion Relays Circuits Status & Address Bar Resizer Control
 */
@Composable
fun TorSecurityTab(viewModel: BrowserViewModel, uiState: BrowserUiState) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Tor circuit map
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SolidSurface),
                shape = RoundedCornerShape(0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(1.dp, SecurityGreen), RoundedCornerShape(0.dp))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Security, contentDescription = "Tor", tint = SecurityGreen)
                            Text("🧅 نظام تور للعبور المشفر (Onion Routing)", color = SecurityGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        
                        Switch(
                            checked = uiState.isTorEnabled,
                            onCheckedChange = { viewModel.setTorEnabled(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = SecurityGreen, checkedTrackColor = SecurityGreen.copy(0.4f))
                        )
                    }

                    Text(
                        text = "يقوم متصفح Research بإخفاء عنوان IP الحقيقي واستبداله بدوائر عشوائية متعددة عبر خوادم تور العالمية، مما يضمن صعوبة التتبع والمراقبة تماماً.",
                        color = TextPrimary,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )

                    Divider(color = LightSurface)

                    Text("🎯 دائرة تور النشطة (Active Tor Relay Map):", color = AccentPurple, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    
                    // Render 3 hops
                    uiState.torCircuit.forEachIndexed { index, hop ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Text(
                                text = "العقدة ${index + 1}:",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = hop,
                                color = TextPrimary,
                                fontSize = 11.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("عنوان الـ IP العشوائي الحالي:", color = TextSecondary, fontSize = 11.sp)
                        Text(uiState.torIp, color = SecurityGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                    }

                    Button(
                        onClick = { viewModel.rotateTorCircuit() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = LightSurface),
                        shape = RoundedCornerShape(0.dp)
                    ) {
                        Icon(Icons.Default.SyncAlt, contentDescription = "Refresh Circuit", tint = AccentTeal)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("توليد دوائر وقنوات عشوائية جديدة 🧅", fontSize = 11.sp, color = AccentTeal)
                    }
                }
            }
        }

        // Safety controls
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SolidSurface),
                shape = RoundedCornerShape(0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("🛡️ مستويات الحماية والتشفير:", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)

                    listOf("Standard (افتراضي مع حماية)", "Safer (شديد الحماية)", "Safest (الخصوصية القصوى)").forEach { level ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.setTorSecurityLevel(level) }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            RadioButton(
                                selected = uiState.torSecurityLevel == level,
                                onClick = { viewModel.setTorSecurityLevel(level) },
                                colors = RadioButtonDefaults.colors(selectedColor = SecurityGreen)
                            )
                            Text(level, color = TextPrimary, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // ADDRESS BAR RESIZER CONTROL PANEL (ميزة تغيير حجم وتصميم شريط العناوين)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SolidSurface),
                shape = RoundedCornerShape(0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("📏 تخصيص وتعديل أبعاد شريط العناوين المستطيل", color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(
                        text = "تحكم تماماً بنسبة عرض وارتفاع شريط كتابة الروابط لتلائم شاشتك وراحتك في التصفح.",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )

                    // Height Slider
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("ارتفاع ومساحة شريط عنوان الموقع (Height):", color = TextPrimary, fontSize = 11.sp)
                            Text("${uiState.addressBarHeightDp} dp", color = AccentPurple, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = uiState.addressBarHeightDp.toFloat(),
                            onValueChange = { newVal ->
                                viewModel.updateAddressBarDimensions(
                                    heightDp = newVal.toInt(),
                                    cornerRadiusDp = uiState.addressBarCornerRadiusDp,
                                    paddingDp = uiState.addressBarHorizontalPaddingDp
                                )
                            },
                            valueRange = 40f..76f,
                            colors = SliderDefaults.colors(thumbColor = AccentPurple, activeTrackColor = AccentPurple)
                        )
                    }

                    // Radius Slider (Square 0dp up to rounded)
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("انحناء الزوايا (Corner Radius - 0 يعني مربع ومستطيل تماماً):", color = TextPrimary, fontSize = 11.sp)
                            Text(
                                text = if (uiState.addressBarCornerRadiusDp == 0) "0 dp (مستطيل مربع 📐)" else "${uiState.addressBarCornerRadiusDp} dp",
                                color = AccentPurple,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Slider(
                            value = uiState.addressBarCornerRadiusDp.toFloat(),
                            onValueChange = { newVal ->
                                viewModel.updateAddressBarDimensions(
                                    heightDp = uiState.addressBarHeightDp,
                                    cornerRadiusDp = newVal.toInt(),
                                    paddingDp = uiState.addressBarHorizontalPaddingDp
                                )
                            },
                            valueRange = 0f..24f,
                            colors = SliderDefaults.colors(thumbColor = AccentPurple, activeTrackColor = AccentPurple)
                        )
                    }

                    // Padding Wide / Narrow Slider
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("الهوامش الجانبية (عرض وحجم الشريط):", color = TextPrimary, fontSize = 11.sp)
                            Text("${uiState.addressBarHorizontalPaddingDp} dp", color = AccentPurple, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = uiState.addressBarHorizontalPaddingDp.toFloat(),
                            onValueChange = { newVal ->
                                viewModel.updateAddressBarDimensions(
                                    heightDp = uiState.addressBarHeightDp,
                                    cornerRadiusDp = uiState.addressBarCornerRadiusDp,
                                    paddingDp = newVal.toInt()
                                )
                            },
                            valueRange = 0f..24f,
                            colors = SliderDefaults.colors(thumbColor = AccentPurple, activeTrackColor = AccentPurple)
                        )
                    }
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebBrowserView(
    activeUrl: String,
    onPageStarted: (String) -> Unit,
    onPageFinished: (String, String) -> Unit,
    onWebViewCreated: (WebView) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    // Safe cleanup of cached states to avoid corrupted directory index/enumerator errors
                    clearCache(true)
                    
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        databaseEnabled = true
                        builtInZoomControls = true
                        displayZoomControls = false
                        useWideViewPort = true
                        loadWithOverviewMode = true
                        setSupportZoom(true)
                        cacheMode = android.webkit.WebSettings.LOAD_DEFAULT
                    }
                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            super.onPageStarted(view, url, favicon)
                            url?.let { onPageStarted(it) }
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            url?.let { onPageFinished(it, view?.title ?: "") }
                        }
                    }
                    webChromeClient = object : WebChromeClient() {}
                    onWebViewCreated(this)
                    loadUrl(activeUrl)
                }
            },
            update = { webView ->
                if (webView.url != activeUrl) {
                    webView.loadUrl(activeUrl)
                }
            }
        )
    }
}

@Composable
fun ListEmptyState(title: String, hint: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Inbox,
                contentDescription = "Empty",
                tint = TextSecondary,
                modifier = Modifier.size(40.dp)
            )
            Text(text = title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(text = hint, color = TextSecondary, fontSize = 11.sp, textAlign = TextAlign.Center, lineHeight = 16.sp)
        }
    }
}

@Composable
fun BrowserBottomBar(
    currentTab: BrowserTab,
    onTabSelected: (BrowserTab) -> Unit,
    historyCount: Int
) {
    NavigationBar(
        containerColor = Color(0xFF101012),
        modifier = Modifier.fillMaxWidth()
    ) {
        NavigationBarItem(
            selected = currentTab == BrowserTab.BROWSER,
            onClick = { onTabSelected(BrowserTab.BROWSER) },
            icon = { Icon(imageVector = Icons.Default.Language, contentDescription = "Browser") },
            label = { Text("المتصفح", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = SecurityGreen,
                selectedTextColor = SecurityGreen,
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary,
                indicatorColor = SecurityGreen.copy(alpha = 0.15f)
            )
        )

        NavigationBarItem(
            selected = currentTab == BrowserTab.WALLET_REWARDS,
            onClick = { onTabSelected(BrowserTab.WALLET_REWARDS) },
            icon = { Icon(imageVector = Icons.Default.AccountBalanceWallet, contentDescription = "Wallet") },
            label = { Text("المحفظة والأرباح", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = AccentTeal,
                selectedTextColor = AccentTeal,
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary,
                indicatorColor = AccentTeal.copy(alpha = 0.15f)
            )
        )

        NavigationBarItem(
            selected = currentTab == BrowserTab.DOWNLOADS,
            onClick = { onTabSelected(BrowserTab.DOWNLOADS) },
            icon = { Icon(imageVector = Icons.Default.FileDownload, contentDescription = "Downloads") },
            label = { Text("التنزيلات", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = AccentPurple,
                selectedTextColor = AccentPurple,
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary,
                indicatorColor = AccentPurple.copy(alpha = 0.15f)
            )
        )

        NavigationBarItem(
            selected = currentTab == BrowserTab.TAKEOUT_CLOUD,
            onClick = { onTabSelected(BrowserTab.TAKEOUT_CLOUD) },
            icon = { Icon(imageVector = Icons.Default.CloudQueue, contentDescription = "Takeout") },
            label = { Text("السحابة Cloud", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = AccentTeal,
                selectedTextColor = AccentTeal,
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary,
                indicatorColor = AccentTeal.copy(alpha = 0.15f)
            )
        )

        NavigationBarItem(
            selected = currentTab == BrowserTab.TOR_SECURITY,
            onClick = { onTabSelected(BrowserTab.TOR_SECURITY) },
            icon = { Icon(imageVector = Icons.Default.VpnKey, contentDescription = "Tor Engine") },
            label = { Text("شبكة تور", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = SecurityGreen,
                selectedTextColor = SecurityGreen,
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary,
                indicatorColor = SecurityGreen.copy(alpha = 0.15f)
            )
        )
    }
}

// Helper colors
fun AccordionGoldBorderColor() = AccentGold

@Composable
fun SavedResearchWorkspace(viewModel: BrowserViewModel, uiState: BrowserUiState) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Workspace Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🧠 مساعد البحث العلمي AI Research",
                color = AccentPurple,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Button(
                onClick = { viewModel.setTab(BrowserTab.BROWSER) },
                colors = ButtonDefaults.buttonColors(containerColor = LightSurface),
                shape = RoundedCornerShape(0.dp)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("المتصفح", fontSize = 11.sp, color = TextPrimary)
            }
        }

        if (uiState.isResearching) {
            // Generating / Loading view
            Card(
                colors = CardDefaults.cardColors(containerColor = SolidSurface),
                shape = RoundedCornerShape(0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .border(BorderStroke(1.dp, AccentTeal), RoundedCornerShape(0.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(color = AccentTeal, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "جاري إعداد التقرير الأكاديمي الشامل...",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "الموضوع: \"${uiState.researchTopic}\"",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Cute simulated logs
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("📡 جاري البحث في قواعد البيانات العلمية المشفرة...", color = TextSecondary, fontSize = 11.sp)
                        Text("📐 فلترة الأطروحات وتصنيف البيانات...", color = TextSecondary, fontSize = 11.sp)
                        Text("🧙 صياغة البحث التلقائي بواسطة مساعد Gemini AI...", color = AccentPurple, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            // Report output view
            Card(
                colors = CardDefaults.cardColors(containerColor = SolidSurface),
                shape = RoundedCornerShape(0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .border(BorderStroke(1.dp, LightSurface), RoundedCornerShape(0.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "تقرير الموضوع: ${uiState.researchTopic}",
                            color = AccentGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Divider(color = LightSurface, modifier = Modifier.padding(vertical = 10.dp))

                    if (uiState.researchError != null) {
                        Text(
                            text = uiState.researchError ?: "",
                            color = Color.Red,
                            fontSize = 12.sp
                        )
                    } else if (uiState.researchResult.isNotBlank()) {
                        LazyColumn(modifier = Modifier.weight(1f)) {
                            item {
                                Text(
                                    text = uiState.researchResult,
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    lineHeight = 20.sp,
                                    textAlign = TextAlign.Right
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "لم يتم إجراء أي بحث بعد. يمكنك كتابة كلمة في المتصفح والضغط على 'بحوث ذكية AI' للبدء في إنتاج التقرير المعزز.",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    if (uiState.researchResult.isNotBlank()) {
                        Divider(color = LightSurface, modifier = Modifier.padding(vertical = 10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    clipboard.setText(AnnotatedString(uiState.researchResult))
                                    Toast.makeText(context, "تم نسخ التقرير البحثي كاملاً!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = LightSurface),
                                shape = RoundedCornerShape(0.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = AccentTeal)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("نسخ التقرير", fontSize = 11.sp, color = AccentTeal)
                            }

                            Button(
                                onClick = {
                                    // Generate simulated download file
                                    viewModel.triggerSimulatedDownload(
                                        fileName = "تقرير بحثي - ${uiState.researchTopic.take(15)}.pdf",
                                        sourceUrl = "AI Research Hub"
                                    )
                                    Toast.makeText(context, "تم حفظ التقرير العلمي للتحميلات بنجاح!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1.2f),
                                colors = ButtonDefaults.buttonColors(containerColor = SecurityGreen),
                                shape = RoundedCornerShape(0.dp)
                            ) {
                                Icon(Icons.Default.Save, contentDescription = "Save Draft", tint = DarkBg)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("تنزيل كـ PDF 💾", fontSize = 11.sp, color = DarkBg, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
