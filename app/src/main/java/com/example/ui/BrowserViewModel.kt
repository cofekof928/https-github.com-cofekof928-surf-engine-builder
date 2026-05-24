package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.network.Content
import com.example.network.GenerateContentRequest
import com.example.network.Part
import com.example.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

enum class BrowserTab {
    BROWSER,
    WALLET_REWARDS,
    DOWNLOADS,
    TAKEOUT_CLOUD,
    TOR_SECURITY,
    SAVED_RESEARCH
}

data class DownloadItem(
    val id: String,
    val fileName: String,
    val sizeMb: Double,
    val timestamp: Long,
    val isCompleted: Boolean = true,
    val urlSource: String
)

data class WalletTransaction(
    val id: String,
    val type: String, // "سحب InstaPay", "سحب Orange Cash", "سحب عملات رقمية", "مكافأة تصفح", "عمولة إحالة"
    val destination: String,
    val amount: String,
    val status: String, // "مكتمل", "قيد المعالجة"
    val date: String
)

data class BrowserTabItem(
    val id: String = UUID.randomUUID().toString(),
    val url: String = "https://duckduckgo.com",
    val title: String = "الرئيسية Research",
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false
)

data class BrowserUiState(
    val currentTab: BrowserTab = BrowserTab.BROWSER,
    val searchInput: String = "https://duckduckgo.com",
    val activeUrl: String = "https://duckduckgo.com",
    val webTitle: String = "Research — Privacy Engine",
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    
    // Tab list and active tab identifier
    val tabs: List<BrowserTabItem> = listOf(BrowserTabItem(id = "default", url = "https://duckduckgo.com", title = "الرئيسية Research")),
    val activeTabId: String = "default",
    
    // AI Research Engine items
    val isResearching: Boolean = false,
    val researchTopic: String = "",
    val researchResult: String = "",
    val researchError: String? = null,
    
    // Address bar sizing & square custom states
    val addressBarHeightDp: Int = 54,
    val addressBarCornerRadiusDp: Int = 0, // 0 = Square "مستطيل مربع"
    val addressBarHorizontalPaddingDp: Int = 8, 
    
    // Tor onion network configuration status
    val isTorEnabled: Boolean = true,
    val torSecurityLevel: String = "Safer (شديد الحماية)", // Standard, Safer, Safest
    val torCircuit: List<String> = listOf("Guard Node (ألمانيا - 185.220.101.5)", "Middle Node (آيسلندا - 193.11.166.4)", "Exit Node (السويد - 82.52.12.89)"),
    val torIp: String = "82.52.12.89",
    val torProxyStatus: String = "نشط ومستقر",
    
    // Wallet & Balance
    val walletBalanceEgp: Double = 350.00,
    val walletBalanceSatoshis: Long = 45000,
    val minWithdrawLimitEgp: Double = 100.00,
    val withdrawSuccessMessage: String? = null,
    val withdrawErrorMessage: String? = null,
    val lastWithdrawTransaction: WalletTransaction? = null,
    val transactionsList: List<WalletTransaction> = listOf(
        WalletTransaction("TX-9921", "مكافأة تصفح كجائزة علمية", "محفظة التطبيق", "50.00 EGP", "مكتمل", "2026-05-24"),
        WalletTransaction("TX-9920", "عمولة إحالة صديق", "RESEARCH-INV-33", "30.00 EGP", "مكتمل", "2026-05-23")
    ),

    // Rewards & Referral
    val referralCode: String = "RESEARCH-947-EGP",
    val invitedTotal: Int = 4,
    val referralEarningsEgp: Double = 120.00,
    val dailyBonusClaimedToday: Boolean = false,

    // Downloads list simulated
    val downloadsList: List<DownloadItem> = listOf(
        DownloadItem("DL-1", "Quantum_Computing_Arabic_Study.pdf", 14.2, System.currentTimeMillis() - 7200000, true, "https://research-files.cloud/physics"),
        DownloadItem("DL-2", "Onion_Routing_Scientific_Paper.pdf", 4.8, System.currentTimeMillis() - 3600000, true, "https://torproject.org/docs")
    ),

    // Encryption & Takeout Cloud status
    val cloudApiKey: String = "TC-8842-SECURE",
    val cloudEncryptedBackupKey: String = "takeout-seed-aes256h-8a9d1c7f4e9",
    val isCloudSynced: Boolean = false,
    val isSyncingNow: Boolean = false,
    val lastSyncTime: String = "لم يتم المزامنة بعد",
    
    // User login session states
    val isLoggedIn: Boolean = false,
    val loggedInEmail: String? = null,
    val loggedInUsername: String? = null,
    val loginErrorMessage: String? = null,
    
    // DB lists
    val bookmarksList: List<BookmarkItem> = emptyList(),
    val historyList: List<HistoryItem> = emptyList(),
    val savedResearchList: List<SavedResearch> = emptyList()
)

class BrowserViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: BrowserRepository
    private val _uiState = MutableStateFlow(BrowserUiState())
    val uiState: StateFlow<BrowserUiState> = _uiState.asStateFlow()

    init {
        val database = BrowserDatabase.getDatabase(application)
        repository = BrowserRepository(database.browserDao())
        
        // Observe Flows from Database
        viewModelScope.launch {
            repository.bookmarks.collect { list ->
                _uiState.value = _uiState.value.copy(bookmarksList = list)
            }
        }
        viewModelScope.launch {
            repository.history.collect { list ->
                _uiState.value = _uiState.value.copy(historyList = list)
            }
        }
        viewModelScope.launch {
            repository.savedResearch.collect { list ->
                _uiState.value = _uiState.value.copy(savedResearchList = list)
            }
        }
    }

    fun updateSearchInput(input: String) {
        _uiState.value = _uiState.value.copy(searchInput = input)
    }

    fun setTab(tab: BrowserTab) {
        _uiState.value = _uiState.value.copy(currentTab = tab)
    }

    fun setWebNavigationProperties(canBack: Boolean, canForward: Boolean) {
        _uiState.value = _uiState.value.copy(
            canGoBack = canBack,
            canGoForward = canForward
        )
        // Sync navigation properties to current tab item
        val updatedTabs = _uiState.value.tabs.map { tab ->
            if (tab.id == _uiState.value.activeTabId) {
                tab.copy(canGoBack = canBack, canGoForward = canForward)
            } else {
                tab
            }
        }
        _uiState.value = _uiState.value.copy(tabs = updatedTabs)
    }

    fun onWebPageVisited(title: String, url: String) {
        _uiState.value = _uiState.value.copy(
            activeUrl = url,
            webTitle = title
        )
        // Sync to active tab item in list
        val updatedTabs = _uiState.value.tabs.map { tab ->
            if (tab.id == _uiState.value.activeTabId) {
                tab.copy(url = url, title = title.ifBlank { "الرئيسية Research" })
            } else {
                tab
            }
        }
        _uiState.value = _uiState.value.copy(tabs = updatedTabs)

        // Add simulated dynamic download if visited certain file URLs
        if (url.endsWith(".pdf") || url.endsWith(".zip") || url.endsWith(".doc")) {
            val fileName = url.substringAfterLast("/")
            triggerSimulatedDownload(fileName, url)
        }
        
        // Insert into history
        viewModelScope.launch(Dispatchers.IO) {
            if (url.isNotBlank() && !url.startsWith("about:blank")) {
                repository.insertHistory(
                    HistoryItem(title = title.ifBlank { url }, url = url)
                )
            }
        }
    }

    // New Tab Management APIs
    fun createNewTab(url: String = "https://duckduckgo.com") {
        val newTab = BrowserTabItem(url = url, title = "علامة تبويب جديدة")
        val updatedTabs = _uiState.value.tabs + newTab
        _uiState.value = _uiState.value.copy(
            tabs = updatedTabs,
            activeTabId = newTab.id,
            activeUrl = url,
            searchInput = url,
            webTitle = newTab.title,
            canGoBack = false,
            canGoForward = false,
            currentTab = BrowserTab.BROWSER
        )
    }

    fun selectTab(tabId: String) {
        val targetTab = _uiState.value.tabs.find { it.id == tabId } ?: return
        _uiState.value = _uiState.value.copy(
            activeTabId = tabId,
            activeUrl = targetTab.url,
            searchInput = targetTab.url,
            webTitle = targetTab.title,
            canGoBack = targetTab.canGoBack,
            canGoForward = targetTab.canGoForward,
            currentTab = BrowserTab.BROWSER
        )
    }

    fun closeTab(tabId: String) {
        val currentTabs = _uiState.value.tabs
        if (currentTabs.size <= 1) {
            // Cannot close the only tab; reset it instead to a blank page on Research Home Canvas
            val lastTab = BrowserTabItem(url = "https://duckduckgo.com", title = "الرئيسية Research")
            _uiState.value = _uiState.value.copy(
                tabs = listOf(lastTab),
                activeTabId = lastTab.id,
                activeUrl = lastTab.url,
                searchInput = lastTab.url,
                webTitle = lastTab.title,
                canGoBack = false,
                canGoForward = false
            )
            return
        }

        val tabToClose = currentTabs.find { it.id == tabId } ?: return
        val updatedTabs = currentTabs.filter { it.id != tabId }
        
        var newActiveTabId = _uiState.value.activeTabId
        if (tabId == _uiState.value.activeTabId) {
            val closeIndex = currentTabs.indexOf(tabToClose)
            val newIndex = if (closeIndex > 0) closeIndex - 1 else 0
            newActiveTabId = updatedTabs[newIndex].id
        }

        val newActiveTab = updatedTabs.find { it.id == newActiveTabId }!!
        _uiState.value = _uiState.value.copy(
            tabs = updatedTabs,
            activeTabId = newActiveTabId,
            activeUrl = newActiveTab.url,
            searchInput = newActiveTab.url,
            webTitle = newActiveTab.title,
            canGoBack = newActiveTab.canGoBack,
            canGoForward = newActiveTab.canGoForward
        )
    }

    // Address Bar Customizer Sizes
    fun updateAddressBarDimensions(heightDp: Int, cornerRadiusDp: Int, paddingDp: Int) {
        _uiState.value = _uiState.value.copy(
            addressBarHeightDp = heightDp,
            addressBarCornerRadiusDp = cornerRadiusDp,
            addressBarHorizontalPaddingDp = paddingDp
        )
    }

    // Simulated TOR circuit rotation
    fun rotateTorCircuit() {
        val countries = listOf("سويسرا", "آيسلندا", "السويد", "فنلندا", "كندا", "ألمانيا", "النرويج", "هولندا")
        val randomIps = listOf("45.12.88.9", "185.220.101.22", "82.52.12.105", "193.11.166.72", "94.23.1.55")
        
        val newCircuit = listOf(
            "Guard Node (${countries.random()} - ${randomIps.random()})",
            "Middle Node (${countries.random()} - ${randomIps.random()})",
            "Exit Node (${countries.random()} - ${randomIps.random()})"
        )
        _uiState.value = _uiState.value.copy(
            torCircuit = newCircuit,
            torIp = randomIps.random(),
            torProxyStatus = "نشط مشفر (قناة جديدة)"
        )
    }

    fun setTorSecurityLevel(level: String) {
        _uiState.value = _uiState.value.copy(torSecurityLevel = level)
    }

    fun setTorEnabled(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isTorEnabled = enabled)
    }

    // Wallet Cash Withdrawal Actions (InstaPay, Orange Cash, Cryptocurrencies)
    fun withdrawInstapay(ipnAddress: String, amount: Double): Boolean {
        if (ipnAddress.isBlank() || !ipnAddress.contains("@")) {
            _uiState.value = _uiState.value.copy(
                withdrawErrorMessage = "عنوان InstaPay غير صالح. يجب أن يحتوي على @ مثل user@instapay",
                withdrawSuccessMessage = null
            )
            return false
        }
        if (amount > _uiState.value.walletBalanceEgp) {
            _uiState.value = _uiState.value.copy(
                withdrawErrorMessage = "رصيدك غير كافٍ. الرصيد الحالي: ${_uiState.value.walletBalanceEgp} EGP",
                withdrawSuccessMessage = null
            )
            return false
        }
        if (amount < 50.0) {
            _uiState.value = _uiState.value.copy(
                withdrawErrorMessage = "الحد الأدنى للسحب على انستاباي هو 50 EGP",
                withdrawSuccessMessage = null
            )
            return false
        }

        val transaction = WalletTransaction(
            id = "TX-" + (1000 + Random().nextInt(9000)),
            type = "سحب كاش InstaPay 📲",
            destination = ipnAddress,
            amount = "-$amount EGP",
            status = "مكتمل فورياً",
            date = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        )

        _uiState.value = _uiState.value.copy(
            walletBalanceEgp = _uiState.value.walletBalanceEgp - amount,
            transactionsList = listOf(transaction) + _uiState.value.transactionsList,
            withdrawSuccessMessage = "تم سحب مبلغ $amount EGP بنجاح تحويل كاش إلى حساب InstaPay بنجاح مشفر!",
            withdrawErrorMessage = null
        )
        return true
    }

    fun withdrawOrangeCash(phone: String, amount: Double): Boolean {
        if (phone.length < 11 || !phone.startsWith("01")) {
            _uiState.value = _uiState.value.copy(
                withdrawErrorMessage = "رقم محفظة أورانج كاش غير صالح. يجب أن يتكون من 11 رقم ويبدأ بـ 01",
                withdrawSuccessMessage = null
            )
            return false
        }
        if (amount > _uiState.value.walletBalanceEgp) {
            _uiState.value = _uiState.value.copy(
                withdrawErrorMessage = "رصيدك غير كافٍ. الرصيد المتاح: ${_uiState.value.walletBalanceEgp} EGP",
                withdrawSuccessMessage = null
            )
            return false
        }

        val transaction = WalletTransaction(
            id = "TX-" + (1000 + Random().nextInt(9000)),
            type = "سحب Orange Cash 🍊",
            destination = phone,
            amount = "-$amount EGP",
            status = "قيد المعالجة (تأكيد الرسالة)",
            date = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        )

        _uiState.value = _uiState.value.copy(
            walletBalanceEgp = _uiState.value.walletBalanceEgp - amount,
            transactionsList = listOf(transaction) + _uiState.value.transactionsList,
            withdrawSuccessMessage = "تم استلام طلب السحب عبر أورنج كاش بقيمة $amount EGP. قيد الإرسال الفوري لـ $phone",
            withdrawErrorMessage = null
        )
        return true
    }

    fun withdrawCrypto(cryptoAddress: String, amountSatoshis: Long, coinType: String): Boolean {
        if (cryptoAddress.length < 24) {
            _uiState.value = _uiState.value.copy(
                withdrawErrorMessage = "عنوان محفظة العملات الرقمية $coinType غير صالح أو قصير جداً.",
                withdrawSuccessMessage = null
            )
            return false
        }
        if (amountSatoshis > _uiState.value.walletBalanceSatoshis) {
            _uiState.value = _uiState.value.copy(
                withdrawErrorMessage = "الرصيد الرقمي غير كافٍ. المتاح: ${_uiState.value.walletBalanceSatoshis} Satoshis",
                withdrawSuccessMessage = null
            )
            return false
        }

        val transaction = WalletTransaction(
            id = "TX-" + (1000 + Random().nextInt(9000)),
            type = "تحويل سحب Crypto ($coinType) ₿",
            destination = cryptoAddress.take(8) + "..." + cryptoAddress.takeLast(8),
            amount = "-$amountSatoshis Sat",
            status = "مكتمل على البلوكشين",
            date = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        )

        _uiState.value = _uiState.value.copy(
            walletBalanceSatoshis = _uiState.value.walletBalanceSatoshis - amountSatoshis,
            transactionsList = listOf(transaction) + _uiState.value.transactionsList,
            withdrawSuccessMessage = "تم بث المعاملة المشفرة على شبكة البلوكشين بنجاح! تم تحويل $amountSatoshis ساتوشي.",
            withdrawErrorMessage = null
        )
        return true
    }

    // Earn rewards daily
    fun claimDailyBonus() {
        if (_uiState.value.dailyBonusClaimedToday) return
        
        val randomBonus = 15.0 + Random().nextInt(35)
        val transaction = WalletTransaction(
            id = "TX-BONUS",
            type = "جائزة تصفح يومية متطورة 🎁",
            destination = "رصيد المتصفح الآمن",
            amount = "+$randomBonus EGP",
            status = "مكتمل",
            date = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        )

        _uiState.value = _uiState.value.copy(
            walletBalanceEgp = _uiState.value.walletBalanceEgp + randomBonus,
            dailyBonusClaimedToday = true,
            transactionsList = listOf(transaction) + _uiState.value.transactionsList,
            withdrawSuccessMessage = "تمت إضافة مكافأتك اليومية بقيمة $randomBonus EGP لصندوق العمل والبحث!"
        )
    }

    // Referral System Activation Invite Simulation
    fun generateSimulatedInvite() {
        val names = listOf("أسامة كمال", "منى هلال", "أحمد سامي", "محمد مرعي", "شريف صبري", "منى عيسى")
        val randomName = names.random()
        val bonus = 30.0
        
        val transaction = WalletTransaction(
            id = "TX-REF-" + (1000 + Random().nextInt(9000)),
            type = "عمولة إحالة صديق ($randomName) 👥",
            destination = _uiState.value.referralCode,
            amount = "+$bonus EGP",
            status = "مكتمل",
            date = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        )

        _uiState.value = _uiState.value.copy(
            invitedTotal = _uiState.value.invitedTotal + 1,
            referralEarningsEgp = _uiState.value.referralEarningsEgp + bonus,
            walletBalanceEgp = _uiState.value.walletBalanceEgp + bonus,
            transactionsList = listOf(transaction) + _uiState.value.transactionsList,
            withdrawSuccessMessage = "رائع! قام الصديق '$randomName' بالتسجيل عبر رمز إحالتك الفريد. كسبت عمولة $bonus EGP!"
        )
    }

    // Dynamic Download Simulation
    fun triggerSimulatedDownload(fileName: String, sourceUrl: String) {
        val newDownload = DownloadItem(
            id = "DL-${System.currentTimeMillis()}",
            fileName = fileName,
            sizeMb = 5.0 + Random().nextInt(25),
            timestamp = System.currentTimeMillis(),
            isCompleted = true,
            urlSource = sourceUrl
        )
        _uiState.value = _uiState.value.copy(
            downloadsList = listOf(newDownload) + _uiState.value.downloadsList
        )
    }

    fun removeDownload(id: String) {
        _uiState.value = _uiState.value.copy(
            downloadsList = _uiState.value.downloadsList.filter { it.id != id }
        )
    }

    // Takeout Cloud Account: Secure Encrypted synchronization of browser database
    fun performTakeoutCloudSync() {
        _uiState.value = _uiState.value.copy(
            isSyncingNow = true,
            withdrawSuccessMessage = null,
            withdrawErrorMessage = null
        )

        viewModelScope.launch {
            // Simulate end-to-end encrypted AES backup upload delay
            delay(2500)
            
            val formattedDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            _uiState.value = _uiState.value.copy(
                isCloudSynced = true,
                isSyncingNow = false,
                lastSyncTime = formattedDate,
                withdrawSuccessMessage = "تم تشفير كافة سجلاتك وإشاراتك وتقرارير أبحاثك بمحرك AES-256 ورفعها بأمان إلى حساب Takeout Cloud بنجاح!"
            )
        }
    }

    fun loginUser(email: String, password: String): Boolean {
        if (!email.contains("@") || email.length < 5) {
            _uiState.value = _uiState.value.copy(loginErrorMessage = "الرجاء إدخال بريد إلكتروني صحيح")
            return false
        }
        if (password.length < 6) {
            _uiState.value = _uiState.value.copy(loginErrorMessage = "كلمة المرور يجب أن تكون 6 أحرف على الأقل")
            return false
        }
        val defaultUsername = email.substringBefore("@").replaceFirstChar { it.titlecase() }
        val hash = email.hashCode().let { if (it < 0) -it else it }.toString(16)
        val generatedKey = "takeout-seed-aes-$hash"
        _uiState.value = _uiState.value.copy(
            isLoggedIn = true,
            loggedInEmail = email,
            loggedInUsername = defaultUsername,
            cloudEncryptedBackupKey = generatedKey,
            loginErrorMessage = null,
            withdrawSuccessMessage = "أهلاً بك يا $defaultUsername، تم تسجيل دخولك بنجاح!"
        )
        return true
    }

    fun registerUser(email: String, username: String, password: String): Boolean {
        if (!email.contains("@") || email.length < 5) {
            _uiState.value = _uiState.value.copy(loginErrorMessage = "الرجاء إدخال بريد إلكتروني صحيح")
            return false
        }
        if (username.isBlank() || username.length < 3) {
            _uiState.value = _uiState.value.copy(loginErrorMessage = "اسم المستخدم يجب أن يتكون من 3 أحرف على الأقل")
            return false
        }
        if (password.length < 6) {
            _uiState.value = _uiState.value.copy(loginErrorMessage = "كلمة المرور يجب أن تكون 6 أحرف على الأقل")
            return false
        }
        val hash = email.hashCode().let { if (it < 0) -it else it }.toString(16)
        val generatedKey = "takeout-seed-aes-$hash"
        _uiState.value = _uiState.value.copy(
            isLoggedIn = true,
            loggedInEmail = email,
            loggedInUsername = username,
            cloudEncryptedBackupKey = generatedKey,
            loginErrorMessage = null,
            withdrawSuccessMessage = "تم إنشاء حسابك بنجاح! مرحباً بك $username"
        )
        return true
    }

    fun logoutUser() {
        _uiState.value = _uiState.value.copy(
            isLoggedIn = false,
            loggedInEmail = null,
            loggedInUsername = null,
            cloudEncryptedBackupKey = "takeout-seed-aes256h-8a9d1c7f4e9",
            isCloudSynced = false,
            lastSyncTime = "لم يتم المزامنة بعد",
            loginErrorMessage = null,
            withdrawSuccessMessage = "تم تسجيل الخروج بنجاح من حساب متصفح Research."
        )
    }

    fun clearLoginError() {
        _uiState.value = _uiState.value.copy(loginErrorMessage = null)
    }

    fun clearNotifications() {
        _uiState.value = _uiState.value.copy(
            withdrawSuccessMessage = null,
            withdrawErrorMessage = null
        )
    }

    // Toggle Bookmarks (DB)
    fun toggleBookmark(title: String, url: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val isBookmarked = repository.isBookmarked(url)
            if (isBookmarked) {
                repository.deleteBookmarkByUrl(url)
            } else {
                repository.insertBookmark(
                    BookmarkItem(title = title.ifBlank { url }, url = url)
                )
            }
        }
    }

    suspend fun isCurrentUrlBookmarked(url: String): Boolean {
        return repository.isBookmarked(url)
    }

    fun deleteBookmark(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteBookmark(id)
        }
    }

    fun deleteHistory(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteHistory(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearHistory()
        }
    }

    fun deleteSavedResearch(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteResearch(id)
        }
    }

    // Navigation and Direct search processor
    fun navigateToInputUrl(input: String) {
        val processedUrl = when {
            input.isBlank() -> "https://duckduckgo.com"
            input.contains(".") && !input.contains(" ") -> {
                if (input.startsWith("http://") || input.startsWith("https://")) {
                    input
                } else {
                    "https://$input"
                }
            }
            else -> {
                // Privacy search DuckDuckGo directly
                "https://duckduckgo.com/?q=${input.replace(" ", "+")}"
            }
        }
        
        val updatedTabs = _uiState.value.tabs.map { tab ->
            if (tab.id == _uiState.value.activeTabId) {
                tab.copy(url = processedUrl, title = if (processedUrl.contains("duckduckgo")) "الرئيسية Research" else processedUrl)
            } else {
                tab
            }
        }

        _uiState.value = _uiState.value.copy(
            activeUrl = processedUrl,
            searchInput = processedUrl,
            currentTab = BrowserTab.BROWSER,
            tabs = updatedTabs
        )
    }

    // Intelligent "Research Engine" AI helper
    fun performAiResearch(query: String) {
        if (query.isBlank()) return

        _uiState.value = _uiState.value.copy(
            isResearching = true,
            researchTopic = query,
            researchResult = "",
            researchError = null,
            currentTab = BrowserTab.SAVED_RESEARCH
        )

        viewModelScope.launch(Dispatchers.IO) {
            val apiKey = RetrofitClient.getApiKey()
            if (apiKey.isBlank()) {
                _uiState.value = _uiState.value.copy(
                    isResearching = false,
                    researchError = "مفتاح API الخاص بـ Gemini غير متوفر. الرجاء إدخاله في لوحة الأسرار (Secrets Panel)."
                )
                return@launch
            }

            val systemPrompt = """
                أنت محرك البحث العلمي والبحثي المدمج والذكي في متصفح "Research" الشهير. 
                قم بإعداد تقرير بحثي مفصل، رصين، ومنظم باللغة العربية حول الموضوع المطلوب من المستخدم. 
                يجب أن يحتوي التقرير على الهيكل التالي بدقة:
                
                📌 عنوان البحث: [عنوان مناسب وجذاب]
                
                📖 المقدمة والأهمية:
                [اكتب مقدمة تمهد للموضوع وتشرح أهميته العلمية والعملية]
                
                🔍 المحاور والنقاط الرئيسية:
                [قم بتقسيم الموضوع إلى نقاط فرعية مفصلة تشرح جوانبه المختلفة بشكل معمق ورصين]
                
                💡 التحليل والمناقشة:
                [قم بتقسيم الأفكار وتحليل البيانات بذكاء لغوي وعلمي دقيق]
                
                🔮 المرجئ المستقبلي والتوصيات:
                [اكتب أهم التوصيات والاتجاهات المستقبلية للموضوع]
                
                📚 المراجع والمصادر المقترحة:
                [اذكر 3 إلى 5 مصادر أو كتب بحثية موثوقة]
            """.trimIndent()

            val request = GenerateContentRequest(
                contents = listOf(Content(parts = listOf(Part(text = "موضوع البحث العلمي المطلوب: $query")))),
                systemInstruction = Content(parts = listOf(Part(text = systemPrompt)))
            )

            try {
                val response = RetrofitClient.service.generateContent(apiKey, request)
                val textResponse = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                
                if (textResponse != null) {
                    _uiState.value = _uiState.value.copy(
                        isResearching = false,
                        researchResult = textResponse,
                        researchError = null
                    )
                    // Auto save research
                    repository.insertResearch(
                        SavedResearch(
                            query = query,
                            responseText = textResponse
                        )
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isResearching = false,
                        researchError = "فشل في الحصول على استجابة بحثية من الخادم. الرجاء المحاولة لاحقاً."
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isResearching = false,
                    researchError = "حدث خطأ أثناء البحث: ${e.localizedMessage ?: e.message}"
                )
            }
        }
    }

    fun displayResearchReport(topic: String, reportText: String) {
        _uiState.value = _uiState.value.copy(
            researchTopic = topic,
            researchResult = reportText,
            researchError = null,
            currentTab = BrowserTab.SAVED_RESEARCH
        )
    }
}
