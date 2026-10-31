package io.github.movetakeitblue.iitcexpressive.i18n

object Strings {
    private var currentLanguage: String = "system"

    private val enMap = mapOf(
        "app.name.release" to "IITC Expressive",
        "app.name.beta" to "IITC Expressive Beta",
        "app.name.debug" to "IITC Expressive Debug",

        "dialog.ok" to "OK",
        "dialog.cancel" to "Cancel",
        "dialog.close" to "Close",
        "dialog.error.title" to "Error",

        "demo.welcome.title" to "Demo mode",
        "demo.welcome.message" to "You're exploring IITC Expressive with simulated data. Sign in with an Ingress account to see live data.",

        "settings.title" to "Settings",
        "settings.section.appearance" to "Appearance & Theme",
        "settings.appearance.title" to "Appearance",
        "settings.appearance.description" to "Theme mode, colors, language, and map display",

        "settings.section.iitc_management" to "IITC management",
        "settings.plugins.title" to "Plugins",
        "settings.plugins.description" to "%s plugins enabled",
        "settings.update_channel.title" to "Update channel",
        "settings.update_channel.description" to "Current: %s",

        "settings.section.map" to "Map & Location",
        "settings.location.title" to "Display user location on map",
        "settings.location.description" to "Show position marker and track location",
        "settings.persistent_zoom.title" to "Persistent zoom level",
        "settings.persistent_zoom.description" to "Don't change zoom level when locate button is pressed",

        "settings.section.data" to "Data management",
        "settings.backup.title" to "Backup & restore",
        "settings.backup.description" to "Export or import your data",
        "settings.clear_cookies.title" to "Clear cookies",
        "settings.clear_cookies.description" to "Remove all stored cookies and session data",
        "settings.clear_cookies.dialog.message" to "This will clear all cookies and session data. You will need to log in again. Continue?",
        "settings.clear_cookies.dialog.ok" to "Clear",
        "settings.clear_cookies.error" to "Failed to clear cookies. Please try again.",

        "settings.section.information" to "Information",
        "settings.about.title" to "About",
        "settings.about.description" to "Version and app information",
        "settings.licenses.title" to "Open source licenses",
        "settings.licenses.description" to "View licenses of used libraries",

        // Appearance
        "appearance.title" to "Appearance",
        "appearance.section.theme_mode" to "Theme Mode",
        "appearance.theme.system" to "Follow system",
        "appearance.theme.light" to "Light mode",
        "appearance.theme.dark" to "Dark mode",

        "appearance.section.colors" to "Color Palette",
        "appearance.dynamic_color.title" to "Dynamic theme color (Material You)",
        "appearance.dynamic_color.desc" to "Extract accent color from system wallpaper (Android 12+)",
        "appearance.preset_colors.title" to "Preset accent colors",
        "appearance.color.blue" to "Resistance Blue",
        "appearance.color.green" to "Enlightened Green",
        "appearance.color.red" to "Machina Red",
        "appearance.color.purple" to "Amethyst Purple",
        "appearance.color.amber" to "Amber Orange",
        "appearance.color.teal" to "Aurora Teal",
        "appearance.color.slate" to "Slate Grey",

        "appearance.section.language" to "Language",
        "appearance.language.title" to "App Language",
        "appearance.language.system" to "Follow system",
        "appearance.language.zh_cn" to "中文（简体）",
        "appearance.language.zh_hk" to "中文（香港繁體）",
        "appearance.language.zh_tw" to "中文（台灣繁體）",
        "appearance.language.en" to "English",
        "appearance.language.ja" to "日本語",

        "appearance.section.map_display" to "Map Display & Typography",
        "appearance.fullscreen_map.title" to "Fullscreen map",
        "appearance.fullscreen_map.description" to "Extend map behind the status bar for an immersive view",
        "appearance.dark_map.title" to "Dark map tiles",
        "appearance.dark_map.description" to "Use high-contrast dark theme tiles",
        "appearance.zoom_controls.title" to "Zoom controls (+/-)",
        "appearance.zoom_controls.description" to "Show floating +/- buttons on the map",
        "appearance.map_text_color.title" to "Map text color",
        "appearance.portal_font_size.title" to "Portal name font size",
        "appearance.agent_font_size.title" to "Player codename font size",
        "appearance.font_family.title" to "Map font family",
        "appearance.font_bold.title" to "Bold map text",
        "appearance.text_halo.title" to "Text halo outline",
        "appearance.player_icon_scale.title" to "Player marker scale",

        "nav.settings" to "Settings",
        "nav.plugins" to "Plugins",
        "nav.debug" to "Debug",
        "nav.reload_iitc" to "Reload",
        "nav.back" to "Back",
        "panel.locate" to "Locate",
        "panel.layers" to "Layers",
        "nav.chat" to "Chat",
        "bookmarks.title" to "Bookmarks",
        "portallist.title" to "Portal List",
        "drawtools.title" to "Draw Tools"
    )

    private val zhCnMap = mapOf(
        "app.name.release" to "IITC Expressive",
        "app.name.beta" to "IITC Expressive Beta",
        "app.name.debug" to "IITC Expressive Debug",

        "dialog.ok" to "确定",
        "dialog.cancel" to "取消",
        "dialog.close" to "关闭",
        "dialog.error.title" to "错误",

        "demo.welcome.title" to "演示模式",
        "demo.welcome.message" to "您正在使用模拟数据体验 IITC Expressive。登录 Ingress 账号即可加载实时情报图层。",

        "settings.title" to "设置",
        "settings.section.appearance" to "外观与个性化",
        "settings.appearance.title" to "外观设置",
        "settings.appearance.description" to "应用主题、配色方案、语言及地图排版视觉",

        "settings.section.iitc_management" to "IITC 插件管理",
        "settings.plugins.title" to "插件列表",
        "settings.plugins.description" to "已启用 %s 个插件",
        "settings.update_channel.title" to "更新通道",
        "settings.update_channel.description" to "当前通道：%s",

        "settings.section.map" to "地图与定位",
        "settings.location.title" to "在地图上显示特工定位",
        "settings.location.description" to "展示定位光标并实时跟踪位置",
        "settings.persistent_zoom.title" to "保持缩放级别",
        "settings.persistent_zoom.description" to "点击定位按钮时不强制重置缩放级别",

        "settings.section.data" to "数据管理",
        "settings.backup.title" to "备份与恢复",
        "settings.backup.description" to "导出或导入配置与标绘数据",
        "settings.clear_cookies.title" to "清除 Cookie 与会话",
        "settings.clear_cookies.description" to "清除所有已保存的 Cookie 与登录状态",
        "settings.clear_cookies.dialog.message" to "此操作将清除所有 Cookie 与会话数据，您需要重新登录 Ingress 账号。是否继续？",
        "settings.clear_cookies.dialog.ok" to "清除",
        "settings.clear_cookies.error" to "清除 Cookie 失败，请重试。",

        "settings.section.information" to "关于信息",
        "settings.about.title" to "关于 IITC Expressive",
        "settings.about.description" to "版本信息与项目主页",
        "settings.licenses.title" to "开源许可协议",
        "settings.licenses.description" to "查看所使用开源库的许可证书",

        // Appearance
        "appearance.title" to "外观设置",
        "appearance.section.theme_mode" to "应用主题",
        "appearance.theme.system" to "跟随系统",
        "appearance.theme.light" to "浅色模式",
        "appearance.theme.dark" to "深色模式",

        "appearance.section.colors" to "主题配色",
        "appearance.dynamic_color.title" to "动态主题色 (Material You)",
        "appearance.dynamic_color.desc" to "根据系统壁纸自动提取强调色 (Android 12+)",
        "appearance.preset_colors.title" to "预设强调色",
        "appearance.color.blue" to "Resistance 蓝军蓝",
        "appearance.color.green" to "Enlightened 绿军绿",
        "appearance.color.red" to "Machina 红军红",
        "appearance.color.purple" to "Amethyst 紫晶紫",
        "appearance.color.amber" to "Amber 琥珀橙",
        "appearance.color.teal" to "Aurora 极光青",
        "appearance.color.slate" to "Slate 静谧灰",

        "appearance.section.language" to "语言选择",
        "appearance.language.title" to "应用语言",
        "appearance.language.system" to "跟随系统语言",
        "appearance.language.zh_cn" to "中文（简体）",
        "appearance.language.zh_hk" to "中文（香港繁體）",
        "appearance.language.zh_tw" to "中文（台灣繁體）",
        "appearance.language.en" to "English",
        "appearance.language.ja" to "日本語",

        "appearance.section.map_display" to "地图显示与排版",
        "appearance.fullscreen_map.title" to "全面屏显示地图",
        "appearance.fullscreen_map.description" to "地图直接延伸至状态栏区域，带来沉浸式视野",
        "appearance.dark_map.title" to "深色地图模式",
        "appearance.dark_map.description" to "切换高对比度暗黑地图底图瓦片",
        "appearance.zoom_controls.title" to "缩放控制按钮 (+/-)",
        "appearance.zoom_controls.description" to "在地图右侧显示悬浮 +/- 快捷缩放按钮",
        "appearance.map_text_color.title" to "地图文字颜色",
        "appearance.portal_font_size.title" to "Portal 名称字号",
        "appearance.agent_font_size.title" to "特工代号字号",
        "appearance.font_family.title" to "地图文字字族",
        "appearance.font_bold.title" to "文字粗体显示",
        "appearance.text_halo.title" to "文字光晕描边",
        "appearance.player_icon_scale.title" to "玩家光标缩放比例",

        "nav.settings" to "设置",
        "nav.plugins" to "插件",
        "nav.debug" to "调试",
        "nav.reload_iitc" to "刷新",
        "nav.back" to "返回",
        "panel.locate" to "定位",
        "panel.layers" to "图层",
        "nav.chat" to "聊天",
        "bookmarks.title" to "收藏",
        "portallist.title" to "Portal 列表",
        "drawtools.title" to "标绘工具"
    )

    private val zhHkMap = mapOf(
        "app.name.release" to "IITC Expressive",
        "app.name.beta" to "IITC Expressive Beta",
        "app.name.debug" to "IITC Expressive Debug",

        "dialog.ok" to "確定",
        "dialog.cancel" to "取消",
        "dialog.close" to "關閉",
        "dialog.error.title" to "錯誤",

        "settings.title" to "設定",
        "settings.section.appearance" to "外觀與個人化",
        "settings.appearance.title" to "外觀設定",
        "settings.appearance.description" to "應用程式主題、配色、語言及地圖排版視覺",

        "settings.section.iitc_management" to "IITC 外掛管理",
        "settings.plugins.title" to "外掛列表",
        "settings.plugins.description" to "已啟用 %s 個外掛",
        "settings.update_channel.title" to "更新頻道",
        "settings.update_channel.description" to "目前頻道：%s",

        "settings.section.map" to "地圖與定位",
        "settings.location.title" to "在地圖上顯示特務定位",
        "settings.location.description" to "展示定位標記並即時追蹤位置",
        "settings.persistent_zoom.title" to "保持縮放級別",
        "settings.persistent_zoom.description" to "點擊定位按鈕時不強制重設縮放級別",

        "settings.section.data" to "資料管理",
        "settings.backup.title" to "備份與還原",
        "settings.backup.description" to "匯出或匯入設定與繪圖資料",
        "settings.clear_cookies.title" to "清除 Cookie 與連線階段",
        "settings.clear_cookies.description" to "清除所有已儲存的 Cookie 與登入狀態",
        "settings.clear_cookies.dialog.message" to "此動作將清除所有 Cookie 與連線階段，你需要重新登入 Ingress 帳戶。是否繼續？",
        "settings.clear_cookies.dialog.ok" to "清除",
        "settings.clear_cookies.error" to "清除 Cookie 失敗，請重試。",

        "settings.section.information" to "關於資訊",
        "settings.about.title" to "關於 IITC Expressive",
        "settings.about.description" to "版本資訊與專案首頁",
        "settings.licenses.title" to "開源許可協議",
        "settings.licenses.description" to "查看使用之開源函式庫許可條款",

        // Appearance
        "appearance.title" to "外觀設定",
        "appearance.section.theme_mode" to "應用程式主題",
        "appearance.theme.system" to "跟隨系統",
        "appearance.theme.light" to "淺色模式",
        "appearance.theme.dark" to "深色模式",

        "appearance.section.colors" to "色彩主題",
        "appearance.dynamic_color.title" to "動態主題色彩 (Material You)",
        "appearance.dynamic_color.desc" to "根據系統桌布自動提取強調色彩 (Android 12+)",
        "appearance.preset_colors.title" to "預設強調色彩",
        "appearance.color.blue" to "Resistance 藍軍藍",
        "appearance.color.green" to "Enlightened 綠軍綠",
        "appearance.color.red" to "Machina 紅軍紅",
        "appearance.color.purple" to "Amethyst 紫晶紫",
        "appearance.color.amber" to "Amber 琥珀橙",
        "appearance.color.teal" to "Aurora 極光青",
        "appearance.color.slate" to "Slate 靜謐灰",

        "appearance.section.language" to "語言設定",
        "appearance.language.title" to "應用程式語言",
        "appearance.language.system" to "跟隨系統語言",
        "appearance.language.zh_cn" to "中文（簡體）",
        "appearance.language.zh_hk" to "中文（香港繁體）",
        "appearance.language.zh_tw" to "中文（台灣繁體）",
        "appearance.language.en" to "English",
        "appearance.language.ja" to "日本語",

        "appearance.section.map_display" to "地圖顯示與排版",
        "appearance.fullscreen_map.title" to "全螢幕顯示地圖",
        "appearance.fullscreen_map.description" to "地圖直接延伸至狀態列區域，提供沉浸式視野",
        "appearance.dark_map.title" to "深色地圖模式",
        "appearance.dark_map.description" to "切換高對比深色地圖磚",
        "appearance.zoom_controls.title" to "縮放控制按鈕 (+/-)",
        "appearance.zoom_controls.description" to "在地圖右側顯示浮動 +/- 快捷縮放按鈕",
        "appearance.map_text_color.title" to "地圖文字顏色",
        "appearance.portal_font_size.title" to "Portal 名稱字型大小",
        "appearance.agent_font_size.title" to "特務代號字型大小",
        "appearance.font_family.title" to "地圖文字字族",
        "appearance.font_bold.title" to "文字粗體顯示",
        "appearance.text_halo.title" to "文字光暈描邊",
        "appearance.player_icon_scale.title" to "玩家標記縮放比例",

        "nav.settings" to "設定",
        "nav.plugins" to "外掛",
        "nav.debug" to "除錯",
        "nav.reload_iitc" to "重新載入",
        "nav.back" to "返回",
        "panel.locate" to "定位",
        "panel.layers" to "圖層",
        "nav.chat" to "通訊",
        "bookmarks.title" to "收藏",
        "portallist.title" to "Portal 列表",
        "drawtools.title" to "繪圖工具"
    )

    private val zhTwMap = mapOf(
        "app.name.release" to "IITC Expressive",
        "app.name.beta" to "IITC Expressive Beta",
        "app.name.debug" to "IITC Expressive Debug",

        "dialog.ok" to "確定",
        "dialog.cancel" to "取消",
        "dialog.close" to "關閉",
        "dialog.error.title" to "錯誤",

        "settings.title" to "設定",
        "settings.section.appearance" to "外觀與個人化",
        "settings.appearance.title" to "外觀設定",
        "settings.appearance.description" to "應用程式主題、配色、語言及地圖排版視覺",

        "settings.section.iitc_management" to "IITC 外掛管理",
        "settings.plugins.title" to "外掛列表",
        "settings.plugins.description" to "已啟用 %s 個外掛",
        "settings.update_channel.title" to "更新頻道",
        "settings.update_channel.description" to "目前頻道：%s",

        "settings.section.map" to "地圖與定位",
        "settings.location.title" to "在地圖上顯示探員定位",
        "settings.location.description" to "展示定位標記並即時追蹤位置",
        "settings.persistent_zoom.title" to "保持縮放級別",
        "settings.persistent_zoom.description" to "點擊定位按鈕時不強制重設縮放級別",

        "settings.section.data" to "資料管理",
        "settings.backup.title" to "備份與還原",
        "settings.backup.description" to "匯出或匯入設定與繪圖資料",
        "settings.clear_cookies.title" to "清除 Cookie 與連線階段",
        "settings.clear_cookies.description" to "清除所有已儲存的 Cookie 與登入狀態",
        "settings.clear_cookies.dialog.message" to "此動作將清除所有 Cookie 與連線階段，你需要重新登入 Ingress 帳戶。是否繼續？",
        "settings.clear_cookies.dialog.ok" to "清除",
        "settings.clear_cookies.error" to "清除 Cookie 失敗，請重試。",

        "settings.section.information" to "關於資訊",
        "settings.about.title" to "關於 IITC Expressive",
        "settings.about.description" to "版本資訊與專案首頁",
        "settings.licenses.title" to "開源授權協議",
        "settings.licenses.description" to "查看使用之開源函式庫授權條款",

        // Appearance
        "appearance.title" to "外觀設定",
        "appearance.section.theme_mode" to "應用程式主題",
        "appearance.theme.system" to "跟隨系統",
        "appearance.theme.light" to "淺色模式",
        "appearance.theme.dark" to "深色模式",

        "appearance.section.colors" to "色彩主題",
        "appearance.dynamic_color.title" to "動態主題色彩 (Material You)",
        "appearance.dynamic_color.desc" to "根據系統桌布自動提取強調色彩 (Android 12+)",
        "appearance.preset_colors.title" to "預設強調色彩",
        "appearance.color.blue" to "Resistance 藍軍藍",
        "appearance.color.green" to "Enlightened 綠軍綠",
        "appearance.color.red" to "Machina 紅軍紅",
        "appearance.color.purple" to "Amethyst 紫晶紫",
        "appearance.color.amber" to "Amber 琥珀橙",
        "appearance.color.teal" to "Aurora 極光青",
        "appearance.color.slate" to "Slate 靜謐灰",

        "appearance.section.language" to "語言設定",
        "appearance.language.title" to "應用程式語言",
        "appearance.language.system" to "跟隨系統語言",
        "appearance.language.zh_cn" to "中文（簡體）",
        "appearance.language.zh_hk" to "中文（香港繁體）",
        "appearance.language.zh_tw" to "中文（台灣繁體）",
        "appearance.language.en" to "English",
        "appearance.language.ja" to "日本語",

        "appearance.section.map_display" to "地圖顯示與排版",
        "appearance.fullscreen_map.title" to "全螢幕顯示地圖",
        "appearance.fullscreen_map.description" to "地圖直接延伸至狀態列區域，提供沉浸式視野",
        "appearance.dark_map.title" to "深色地圖模式",
        "appearance.dark_map.description" to "切換高對比深色地圖磚",
        "appearance.zoom_controls.title" to "縮放控制按鈕 (+/-)",
        "appearance.zoom_controls.description" to "在地圖右側顯示浮動 +/- 快捷縮放按鈕",
        "appearance.map_text_color.title" to "地圖文字顏色",
        "appearance.portal_font_size.title" to "Portal 名稱字型大小",
        "appearance.agent_font_size.title" to "探員代號字型大小",
        "appearance.font_family.title" to "地圖文字字族",
        "appearance.font_bold.title" to "文字粗體顯示",
        "appearance.text_halo.title" to "文字光暈描邊",
        "appearance.player_icon_scale.title" to "玩家標記縮放比例",

        "nav.settings" to "設定",
        "nav.plugins" to "外掛",
        "nav.debug" to "除錯",
        "nav.reload_iitc" to "重新載入",
        "nav.back" to "返回",
        "panel.locate" to "定位",
        "panel.layers" to "圖層",
        "nav.chat" to "通訊",
        "bookmarks.title" to "收藏",
        "portallist.title" to "Portal 列表",
        "drawtools.title" to "繪圖工具"
    )

    private val jaMap = mapOf(
        "app.name.release" to "IITC Expressive",
        "app.name.beta" to "IITC Expressive Beta",
        "app.name.debug" to "IITC Expressive Debug",

        "dialog.ok" to "OK",
        "dialog.cancel" to "キャンセル",
        "dialog.close" to "閉じる",
        "dialog.error.title" to "エラー",

        "settings.title" to "設定",
        "settings.section.appearance" to "外観とカスタマイズ",
        "settings.appearance.title" to "外観設定",
        "settings.appearance.description" to "アプリのテーマ、配色、言語、地図表示の調整",

        "settings.section.iitc_management" to "IITC プラグイン管理",
        "settings.plugins.title" to "プラグイン一覧",
        "settings.plugins.description" to "%s 個のプラグインが有効",
        "settings.update_channel.title" to "アップデートチャンネル",
        "settings.update_channel.description" to "現在のチャンネル: %s",

        "settings.section.map" to "地図と位置情報",
        "settings.location.title" to "地図上に現在地を表示",
        "settings.location.description" to "エージェントの位置マーカーを表示しリアルタイム追跡",
        "settings.persistent_zoom.title" to "ズーム倍率を維持",
        "settings.persistent_zoom.description" to "現在地ボタンをタップした際にズーム倍率をリセットしない",

        "settings.section.data" to "データ管理",
        "settings.backup.title" to "バックアップと復元",
        "settings.backup.description" to "設定や描画データのインポート・エクスポート",
        "settings.clear_cookies.title" to "Cookie とセッションを削除",
        "settings.clear_cookies.description" to "保存されたすべての Cookie とログイン情報を消去",
        "settings.clear_cookies.dialog.message" to "すべての Cookie とセッションデータが消去されます。再度ログインが必要になります。続行しますか？",
        "settings.clear_cookies.dialog.ok" to "削除",
        "settings.clear_cookies.error" to "Cookie の削除に失敗しました。もう一度お試しください。",

        "settings.section.information" to "アプリ情報",
        "settings.about.title" to "IITC Expressive について",
        "settings.about.description" to "バージョン情報とプロジェクトリンク",
        "settings.licenses.title" to "オープンソースライセンス",
        "settings.licenses.description" to "使用しているライブラリのライセンス条項",

        // Appearance
        "appearance.title" to "外観設定",
        "appearance.section.theme_mode" to "テーマモード",
        "appearance.theme.system" to "システム設定に従う",
        "appearance.theme.light" to "ライトモード",
        "appearance.theme.dark" to "ダークモード",

        "appearance.section.colors" to "カラーテーマ",
        "appearance.dynamic_color.title" to "ダイナミックカラー (Material You)",
        "appearance.dynamic_color.desc" to "壁紙に基づいてアクセントカラーを自動抽出 (Android 12+)",
        "appearance.preset_colors.title" to "プリセットアクセントカラー",
        "appearance.color.blue" to "Resistance ブルー",
        "appearance.color.green" to "Enlightened グリーン",
        "appearance.color.red" to "Machina レッド",
        "appearance.color.purple" to "Amethyst パープル",
        "appearance.color.amber" to "Amber オレンジ",
        "appearance.color.teal" to "Aurora ティール",
        "appearance.color.slate" to "Slate スレートグレー",

        "appearance.section.language" to "言語設定",
        "appearance.language.title" to "アプリの言語",
        "appearance.language.system" to "システム言語に従う",
        "appearance.language.zh_cn" to "中文（简体）",
        "appearance.language.zh_hk" to "中文（香港繁體）",
        "appearance.language.zh_tw" to "中文（台灣繁體）",
        "appearance.language.en" to "English",
        "appearance.language.ja" to "日本語",

        "appearance.section.map_display" to "地図表示と文字設定",
        "appearance.fullscreen_map.title" to "全画面マップ表示",
        "appearance.fullscreen_map.description" to "ステータスバーの領域まで地図を拡張し没入感を向上",
        "appearance.dark_map.title" to "ダークマップモード",
        "appearance.dark_map.description" to "高コントラストのダークマップタイルを使用",
        "appearance.zoom_controls.title" to "ズームボタン (+/-)",
        "appearance.zoom_controls.description" to "地図の右側にフローティング +/- ボタンを表示",
        "appearance.map_text_color.title" to "地図テキストの色",
        "appearance.portal_font_size.title" to "ポータル名フォントサイズ",
        "appearance.agent_font_size.title" to "エージェント名フォントサイズ",
        "appearance.font_family.title" to "地図フォントファミリー",
        "appearance.font_bold.title" to "文字を太字にする",
        "appearance.text_halo.title" to "文字のアウトライン効果",
        "appearance.player_icon_scale.title" to "プレイヤーマーカーのスケール",

        "nav.settings" to "設定",
        "nav.plugins" to "プラグイン",
        "nav.debug" to "デバッグ",
        "nav.reload_iitc" to "再読み込み",
        "nav.back" to "戻る",
        "panel.locate" to "現在地",
        "panel.layers" to "レイヤー",
        "nav.chat" to "通信",
        "bookmarks.title" to "ブックマーク",
        "portallist.title" to "ポータル一覧",
        "drawtools.title" to "描画ツール"
    )

    fun setLanguage(lang: String) {
        currentLanguage = lang
    }

    fun getLanguage(): String = currentLanguage

    fun get(key: String, vararg args: Any): String {
        val targetMap = when (currentLanguage) {
            "zh-CN", "zh_CN" -> zhCnMap
            "zh-HK", "zh_HK" -> zhHkMap
            "zh-TW", "zh_TW" -> zhTwMap
            "ja" -> jaMap
            "en" -> enMap
            else -> {
                // System fallback: check if platform is Chinese or Japanese
                val sysLang = getSystemLanguageTag()
                when {
                    sysLang.startsWith("zh-HK", ignoreCase = true) || sysLang.startsWith("zh-MO", ignoreCase = true) -> zhHkMap
                    sysLang.startsWith("zh-TW", ignoreCase = true) -> zhTwMap
                    sysLang.startsWith("zh", ignoreCase = true) -> zhCnMap
                    sysLang.startsWith("ja", ignoreCase = true) -> jaMap
                    else -> enMap
                }
            }
        }

        val template = targetMap[key] ?: enMap[key] ?: key
        if (args.isEmpty()) return template
        var result = template
        for (arg in args) {
            result = result.replaceFirst("%s", arg.toString())
        }
        return result
    }

    private fun getSystemLanguageTag(): String {
        return io.github.movetakeitblue.iitcexpressive.platform.PlatformUtils.getSystemLanguage()
    }
}

fun L(key: String, vararg args: Any): String = Strings.get(key, *args)
