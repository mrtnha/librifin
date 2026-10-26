package io.github.mrtnha.librifin.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp
import io.github.mrtnha.librifin.ui.theme.JellyfinBlue
import io.github.mrtnha.librifin.ui.theme.JellyfinPurple

/**
 * The app's icons: Material Symbols Outlined (weight 400 unless noted) by Google, Apache-2.0, see README.
 * Their vector paths are embedded here, so there's no icon library and no network access.
 * New ones: download the SVG from https://fonts.google.com/icons and copy its path (the `d` attribute).
 * Tint them via `Icon(tint = …)`.
 */
object LibrifinIcons {
    /** "arrow_back". Top bar back button. */
    val ArrowBack: ImageVector by lazy {
        symbol("ArrowBack", "m313-440 224 224-57 56-320-320 320-320 57 56-224 224h487v80H313Z")
    }

    /** "check". Badge on the cover of a finished book, like Jellyfin's mark for played items. */
    val Check: ImageVector by lazy {
        symbol("Check", "M382-240 154-468l57-57 171 171 367-367 57 57-424 424Z")
    }

    /** "cloud". Library top bar: all books are shown, the ones on the server too. */
    val Cloud: ImageVector by lazy {
        symbol(
            "Cloud",
            "M260-160q-91 0-155.5-63T40-377q0-78 47-139t123-78q25-92 100-149t170-57q117 0 198.5 81.5T760-520" +
                "q69 8 114.5 59.5T920-340q0 75-52.5 127.5T740-160H260Zm0-80h480q42 0 71-29t29-71q0-42-29-71" +
                "t-71-29h-60v-80q0-83-58.5-141.5T480-720q-83 0-141.5 58.5T280-520h-20q-58 0-99 41t-41 99q0 58 41 99" +
                "t99 41Zm220-240Z",
        )
    }

    /** "cloud_off" (a crossed-out cloud). Library top bar: only the downloaded books are shown. */
    val CloudOff: ImageVector by lazy {
        symbol(
            "CloudOff",
            "M792-56 686-160H260q-92 0-156-64T40-380q0-77 47.5-137T210-594q3-8 6-15.5t6-16.5L56-792l56-56 736 736" +
                "-56 56ZM260-240h346L284-562q-2 11-3 21t-1 21h-20q-58 0-99 41t-41 99q0 58 41 99t99 41Zm185-161Z" +
                "m419 191-58-56q17-14 25.5-32.5T840-340q0-42-29-71t-71-29h-60v-80q0-83-58.5-141.5T480-720" +
                "q-27 0-52 6.5T380-693l-58-58q35-24 74.5-36.5T480-800q117 0 198.5 81.5T760-520q69 8 114.5 59.5" +
                "T920-340q0 39-15 72.5T864-210ZM593-479Z",
        )
    }

    /** "import_contacts" (an open book). Cover placeholder. */
    val Book: ImageVector by lazy { symbol("Book", OPEN_BOOK) }

    /**
     * The open book of [Book] in Jellyfin's gradient: purple at the top left to blue at the bottom right,
     * along the same line as in the launcher icon (ic_launcher_foreground.xml). The app logo; show it with
     * `Image`, as tinting would hide the gradient.
     */
    val Logo: ImageVector by lazy {
        symbol(
            "Logo",
            OPEN_BOOK,
            fill = Brush.linearGradient(
                listOf(JellyfinPurple, JellyfinBlue),
                // In the path's coordinates, see symbol().
                start = Offset(172f, -570f),
                end = Offset(986f, -100f),
            ),
        )
    }

    private const val OPEN_BOOK =
        "M260-320q47 0 91.5 10.5T440-278v-394q-41-24-87-36t-93-12q-36 0-71.5 7T120-692v396q35-12 69.5-18" +
            "t70.5-6Zm260 42q44-21 88.5-31.5T700-320q36 0 70.5 6t69.5 18v-396q-33-14-68.5-21t-71.5-7q-47 0-93 12" +
            "t-87 36v394Zm-40 118q-48-38-104-59t-116-21q-42 0-82.5 11T100-198q-21 11-40.5-1T40-234v-482" +
            "q0-11 5.5-21T62-752q46-24 96-36t102-12q58 0 113.5 15T480-740q51-30 106.5-45T700-800q52 0 102 12" +
            "t96 36q11 5 16.5 15t5.5 21v482q0 23-19.5 35t-40.5 1q-37-20-77.5-31T700-240q-60 0-116 21t-104 59Z" +
            "M280-494Z"

    /** "info". Help next to the server address field, and the settings row with the app's version. */
    val Info: ImageVector by lazy {
        symbol(
            "Info",
            "M440-280h80v-240h-80v240Zm68.5-331.5Q520-623 520-640t-11.5-28.5Q497-680 480-680t-28.5 11.5" +
                "Q440-657 440-640t11.5 28.5Q463-600 480-600t28.5-11.5ZM480-80q-83 0-156-31.5T197-197q-54-54-85.5-127" +
                "T80-480q0-83 31.5-156T197-763q54-54 127-85.5T480-880q83 0 156 31.5T763-763q54 54 85.5 127T880-480" +
                "q0 83-31.5 156T763-197q-54 54-127 85.5T480-80Zm0-80q134 0 227-93t93-227q0-134-93-227t-227-93" +
                "q-134 0-227 93t-93 227q0 134 93 227t227 93Zm0-320Z",
        )
    }

    /** "login". Log in button. */
    val Login: ImageVector by lazy {
        symbol(
            "Login",
            "M480-120v-80h280v-560H480v-80h280q33 0 56.5 23.5T840-760v560q0 33-23.5 56.5T760-120H480Z" +
                "m-80-160-55-58 102-102H120v-80h327L345-622l55-58 200 200-200 200Z",
        )
    }

    /** "logout". Log out button. */
    val Logout: ImageVector by lazy {
        symbol(
            "Logout",
            "M200-120q-33 0-56.5-23.5T120-200v-560q0-33 23.5-56.5T200-840h280v80H200v560h280v80H200Z" +
                "m440-160-55-58 102-102H360v-80h327L585-622l55-58 200 200-200 200Z",
        )
    }

    /** "settings". Settings button in the library top bar. */
    val Settings: ImageVector by lazy {
        symbol(
            "Settings",
            "m370-80-16-128q-13-5-24.5-12T307-235l-119 50L78-375l103-78q-1-7-1-13.5v-27q0-6.5 1-13.5L78-585" +
                "l110-190 119 50q11-8 23-15t24-12l16-128h220l16 128q13 5 24.5 12t22.5 15l119-50 110 190-103 78" +
                "q1 7 1 13.5v27q0 6.5-2 13.5l103 78-110 190-118-50q-11 8-23 15t-24 12L590-80H370Zm70-80h79l14-106" +
                "q31-8 57.5-23.5T639-327l99 41 39-68-86-65q5-14 7-29.5t2-31.5q0-16-2-31.5t-7-29.5l86-65-39-68-99 42" +
                "q-22-23-48.5-38.5T533-694l-13-106h-79l-14 106q-31 8-57.5 23.5T321-633l-99-41-39 68 86 64q-5 15-7 30" +
                "t-2 32q0 16 2 31t7 30l-86 65 39 68 99-42q22 23 48.5 38.5T427-266l13 106Zm42-180q58 0 99-41t41-99" +
                "q0-58-41-99t-99-41q-59 0-99.5 41T342-480q0 58 40.5 99t99.5 41Zm-2-140Z",
        )
    }

    /** "delete" (a trash can). Settings row that removes the downloaded books. */
    val Delete: ImageVector by lazy {
        symbol(
            "Delete",
            "M280-120q-33 0-56.5-23.5T200-200v-520h-40v-80h200v-40h240v40h200v80h-40v520q0 33-23.5 56.5" +
                "T680-120H280Zm400-600H280v520h400v-520ZM360-280h80v-360h-80v360Zm160 0h80v-360h-80v360Z" +
                "M280-720v520-520Z",
        )
    }

    /** "apps" (a grid of nine dots). Settings row that chooses the app icon. */
    val Apps: ImageVector by lazy {
        symbol(
            "Apps",
            "M183.5-183.5Q160-207 160-240t23.5-56.5Q207-320 240-320t56.5 23.5Q320-273 320-240t-23.5 56.5" +
                "Q273-160 240-160t-56.5-23.5Zm240 0Q400-207 400-240t23.5-56.5Q447-320 480-320t56.5 23.5" +
                "Q560-273 560-240t-23.5 56.5Q513-160 480-160t-56.5-23.5Zm240 0Q640-207 640-240t23.5-56.5" +
                "Q687-320 720-320t56.5 23.5Q800-273 800-240t-23.5 56.5Q753-160 720-160t-56.5-23.5Zm-480-240" +
                "Q160-447 160-480t23.5-56.5Q207-560 240-560t56.5 23.5Q320-513 320-480t-23.5 56.5Q273-400 240-400" +
                "t-56.5-23.5Zm240 0Q400-447 400-480t23.5-56.5Q447-560 480-560t56.5 23.5Q560-513 560-480t-23.5 56.5" +
                "Q513-400 480-400t-56.5-23.5Zm240 0Q640-447 640-480t23.5-56.5Q687-560 720-560t56.5 23.5" +
                "Q800-513 800-480t-23.5 56.5Q753-400 720-400t-56.5-23.5Zm-480-240Q160-687 160-720t23.5-56.5" +
                "Q207-800 240-800t56.5 23.5Q320-753 320-720t-23.5 56.5Q273-640 240-640t-56.5-23.5Zm240 0" +
                "Q400-687 400-720t23.5-56.5Q447-800 480-800t56.5 23.5Q560-753 560-720t-23.5 56.5Q513-640 480-640" +
                "t-56.5-23.5Zm240 0Q640-687 640-720t23.5-56.5Q687-800 720-800t56.5 23.5Q800-753 800-720t-23.5 56.5" +
                "Q753-640 720-640t-56.5-23.5Z",
        )
    }

    /** "contrast" (a half-filled circle). Settings row that chooses the app theme. */
    val Contrast: ImageVector by lazy {
        symbol(
            "Contrast",
            "M324-111.5Q251-143 197-197t-85.5-127Q80-397 80-480t31.5-156Q143-709 197-763t127-85.5Q397-880 480-880" +
                "t156 31.5Q709-817 763-763t85.5 127Q880-563 880-480t-31.5 156Q817-251 763-197t-127 85.5" +
                "Q563-80 480-80t-156-31.5ZM520-163q119-15 199.5-104.5T800-480q0-123-80.5-212.5T520-797v634Z",
        )
    }

    /** "chevron_right". End of a settings row that opens something: another screen or a choice. */
    val ChevronRight: ImageVector by lazy {
        symbol("ChevronRight", "M504-480 320-664l56-56 240 240-240 240-56-56 184-184Z")
    }

    /** "description" (a page of text). Open source licenses row in the settings. */
    val Licenses: ImageVector by lazy {
        symbol(
            "Licenses",
            "M320-240h320v-80H320v80Zm0-160h320v-80H320v80ZM240-80q-33 0-56.5-23.5T160-160v-640q0-33 23.5-56.5" +
                "T240-880h320l240 240v480q0 33-23.5 56.5T720-80H240Zm280-520v-200H240v640h480v-440H520Z" +
                "M240-800v200-200 640-640Z",
        )
    }

    /** "account_circle". Settings row with the name of the logged-in user. */
    val Profile: ImageVector by lazy {
        symbol(
            "Profile",
            "M234-276q51-39 114-61.5T480-360q69 0 132 22.5T726-276q35-41 54.5-93T800-480q0-133-93.5-226.5T480-800" +
                "q-133 0-226.5 93.5T160-480q0 59 19.5 111t54.5 93Zm146.5-204.5Q340-521 340-580t40.5-99.5" +
                "Q421-720 480-720t99.5 40.5Q620-639 620-580t-40.5 99.5Q539-440 480-440t-99.5-40.5ZM480-80" +
                "q-83 0-156-31.5T197-197q-54-54-85.5-127T80-480q0-83 31.5-156T197-763q54-54 127-85.5T480-880" +
                "q83 0 156 31.5T763-763q54 54 85.5 127T880-480q0 83-31.5 156T763-197q-54 54-127 85.5T480-80Zm100-95.5" +
                "q47-15.5 86-44.5-39-29-86-44.5T480-280q-53 0-100 15.5T294-220q39 29 86 44.5T480-160q53 0 100-15.5Z" +
                "M523-537q17-17 17-43t-17-43q-17-17-43-17t-43 17q-17 17-17 43t17 43q17 17 43 17t43-17Zm-43-43Zm0 360Z",
        )
    }

    /** "dns" (a server). Settings row with the address of the server the user is logged in to. */
    val Server: ImageVector by lazy {
        symbol(
            "Server",
            "M300-720q-25 0-42.5 17.5T240-660q0 25 17.5 42.5T300-600q25 0 42.5-17.5T360-660q0-25-17.5-42.5" +
                "T300-720Zm0 400q-25 0-42.5 17.5T240-260q0 25 17.5 42.5T300-200q25 0 42.5-17.5T360-260" +
                "q0-25-17.5-42.5T300-320ZM160-840h640q17 0 28.5 11.5T840-800v280q0 17-11.5 28.5T800-480H160" +
                "q-17 0-28.5-11.5T120-520v-280q0-17 11.5-28.5T160-840Zm40 80v200h560v-200H200Zm-40 320h640" +
                "q17 0 28.5 11.5T840-400v280q0 17-11.5 28.5T800-80H160q-17 0-28.5-11.5T120-120v-280" +
                "q0-17 11.5-28.5T160-440Zm40 80v200h560v-200H200Zm0-400v200-200Zm0 400v200-200Z",
        )
    }

    /** "close". Clears the search text. */
    val Close: ImageVector by lazy {
        symbol(
            "Close",
            "m256-200-56-56 224-224-224-224 56-56 224 224 224-224 56 56-224 224 224 224-56 56-224-224-224 224Z",
        )
    }

    /** "search". Search button in the library top bar. */
    val Search: ImageVector by lazy {
        symbol(
            "Search",
            "M784-120 532-372q-30 24-69 38t-83 14q-109 0-184.5-75.5T120-580q0-109 75.5-184.5T380-840" +
                "q109 0 184.5 75.5T640-580q0 44-14 83t-38 69l252 252-56 56ZM380-400q75 0 127.5-52.5T560-580" +
                "q0-75-52.5-127.5T380-760q-75 0-127.5 52.5T200-580q0 75 52.5 127.5T380-400Z",
        )
    }

    /** "sort". Sort button in the library top bar: opens the sheet to choose the order of the books. */
    val Sort: ImageVector by lazy {
        symbol("Sort", "M120-240v-80h240v80H120Zm0-200v-80h480v80H120Zm0-200v-80h720v80H120Z")
    }

    /** "match_case" ("Aa"). Reader app bar: opens the appearance sheet (theme, text size, spacing, font). */
    val MatchCase: ImageVector by lazy {
        symbol(
            "MatchCase",
            "m131-252 165-440h79l165 440h-76l-39-112H247l-40 112h-76Zm139-176h131l-64-182h-4l-63 182Zm395 186" +
                "q-51 0-81-27.5T554-342q0-44 34.5-72.5T677-443q23 0 45 4t38 11v-12q0-29-20.5-47T685-505q-23 0-42 9.5" +
                "T610-468l-47-35q24-29 54.5-43t68.5-14q69 0 103 32.5t34 97.5v178h-63v-37h-4q-14 23-38 35t-53 12Z" +
                "m12-54q35 0 59.5-24t24.5-56q-14-8-33.5-12.5T689-393q-32 0-50 14t-18 37q0 20 16 33t40 13Z",
        )
    }

    /** "text_decrease". Appearance sheet: one text size smaller. */
    val TextDecrease: ImageVector by lazy {
        symbol(
            "TextDecrease",
            "m40-200 210-560h100l210 560h-96l-51-143H187l-51 143H40Zm176-224h168l-82-232h-4l-82 232Zm384-16v-80h320" +
                "v80H600Z",
        )
    }

    /** "text_increase". Appearance sheet: one text size larger. */
    val TextIncrease: ImageVector by lazy {
        symbol(
            "TextIncrease",
            "m40-200 210-560h100l210 560h-96l-51-143H187l-51 143H40Zm176-224h168l-82-232h-4l-82 232Zm504 104v-120H600" +
                "v-80h120v-120h80v120h120v80H800v120h-80Z",
        )
    }

    /** "format_bold" at weight 200, a thin B. Appearance sheet: lighter text. */
    val FormatBoldLight: ImageVector by lazy {
        symbol(
            "FormatBoldLight",
            "M315.08-230v-500H483q57.31 0 101.15 36.15Q628-657.69 628-599.77q0 38.69-20.31 67.73-20.31 29.04" +
                "-50.31 42.58 36.54 10.23 62.04 42.92t25.5 78.85q0 64.38-48.07 101.04Q548.77-230 491-230H315.08Z" +
                "m51-47.38h122.46q46.46 0 75.04-27.97 28.57-27.96 28.57-65.11 0-37.16-28.57-65.12" +
                "-28.58-27.96-75.73-27.96H366.08v186.16Zm0-232.62h114.54" +
                "q39.92 0 67.61-24.69 27.69-24.69 27.69-61.85 0-37.84-28.15-62.08-28.15-24.23-66.69-24.23h-115V-510Z",
        )
    }

    /** "format_bold" at weight 700, a heavy B. Appearance sheet: bolder text. */
    val FormatBoldHeavy: ImageVector by lazy {
        symbol(
            "FormatBoldHeavy",
            "M222-167v-627h303q78 0 135.5 48.5T718-622q0 39-18 74t-53 52v3q44 18 67.5 58t23.5 88q0 79-62 129.5" +
                "T531-167H222Zm155-130h129q28 0 48.5-18t20.5-45q0-27-20.5-45.5T506-424H377v127Z" +
                "m0-254h120q25 0 43.5-16.5T559-608q0-24-18.5-40.5T497-665H377v114Z",
        )
    }

    /** "width_full". Appearance sheet: smaller page margins. */
    val WidthFull: ImageVector by lazy {
        symbol(
            "WidthFull",
            "M160-160q-33 0-56.5-23.5T80-240v-480q0-33 23.5-56.5T160-800h640q33 0 56.5 23.5T880-720v480" +
                "q0 33-23.5 56.5T800-160H160Zm0-80h40v-480h-40v480Zm120 0h400v-480H280v480Zm480 0h40v-480h-40v480Z" +
                "M280-720v480-480Z",
        )
    }

    /** "width_normal". Appearance sheet: larger page margins. */
    val WidthNormal: ImageVector by lazy {
        symbol(
            "WidthNormal",
            "M160-160q-33 0-56.5-23.5T80-240v-480q0-33 23.5-56.5T160-800h640q33 0 56.5 23.5T880-720v480" +
                "q0 33-23.5 56.5T800-160H160Zm0-80h160v-480H160v480Zm240 0h160v-480H400v480Zm240 0h160v-480H640v480Z" +
                "m-240 0v-480 480Z",
        )
    }

    /** "density_small". Appearance sheet: less line spacing. */
    val DensitySmall: ImageVector by lazy {
        symbol(
            "DensitySmall",
            "M120-80v-80h720v80H120Zm0-240v-80h720v80H120Zm0-240v-80h720v80H120Zm0-240v-80h720v80H120Z",
        )
    }

    /** "density_medium". Appearance sheet: more line spacing. */
    val DensityMedium: ImageVector by lazy {
        symbol("DensityMedium", "M120-120v-80h720v80H120Zm0-320v-80h720v80H120Zm0-320v-80h720v80H120Z")
    }

    /** "keyboard_arrow_up". Reader search bar: previous match. */
    val KeyboardArrowUp: ImageVector by lazy {
        symbol("KeyboardArrowUp", "M480-528 296-344l-56-56 240-240 240 240-56 56-184-184Z")
    }

    /** "keyboard_arrow_down". Reader search bar: next match. */
    val KeyboardArrowDown: ImageVector by lazy {
        symbol("KeyboardArrowDown", "M480-344 240-584l56-56 184 184 184-184 56 56-240 240Z")
    }

    /**
     * A 24 dp icon from the path of a Material Symbols SVG. Their viewBox is "0 -960 960 960": 960 units
     * wide and high, from y = -960 at the top to 0 at the bottom, so the path is moved down by 960.
     */
    private fun symbol(name: String, path: String, fill: Brush = SolidColor(Color.Black)): ImageVector =
        ImageVector.Builder(
            name = name,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f,
        ).addGroup(translationY = 960f).addPath(addPathNodes(path), fill = fill).build()
}
