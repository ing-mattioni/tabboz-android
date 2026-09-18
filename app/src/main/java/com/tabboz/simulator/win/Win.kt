@file:Suppress("unused", "FunctionName")

package com.tabboz.simulator.win

// -----------------------------------------------------------------------------
// Costanti Win32 usate dalla logica di gioco (stessi nomi/valori di windows.h)
// -----------------------------------------------------------------------------

const val WM_CREATE = 0x0001
const val WM_DESTROY = 0x0002
const val WM_PAINT = 0x000F
const val WM_CLOSE = 0x0010
const val WM_KEYDOWN = 0x0100
const val WM_INITDIALOG = 0x0110
const val WM_COMMAND = 0x0111
const val WM_SYSCOMMAND = 0x0112
const val WM_TIMER = 0x0113
const val WM_LBUTTONDOWN = 0x0201
const val WM_ENDSESSION = 0x0016

const val SC_CLOSE = 0xF060
const val SC_MINIMIZE = 0xF020

const val IDOK = 1
const val IDCANCEL = 2
const val IDABORT = 3
const val IDRETRY = 4
const val IDIGNORE = 5
const val IDYES = 6
const val IDNO = 7

const val MB_OK = 0x00000000
const val MB_OKCANCEL = 0x00000001
const val MB_YESNO = 0x00000004
const val MB_ICONHAND = 0x00000010
const val MB_ICONSTOP = 0x00000010
const val MB_ICONERROR = 0x00000010
const val MB_ICONQUESTION = 0x00000020
const val MB_ICONEXCLAMATION = 0x00000030
const val MB_ICONWARNING = 0x00000030
const val MB_ICONINFORMATION = 0x00000040
const val MB_ICONASTERISK = 0x00000040

const val SW_HIDE = 0
const val SW_SHOWNORMAL = 1

fun LOWORD(v: Int): Int = v and 0xFFFF
fun HIWORD(v: Int): Int = (v ushr 16) and 0xFFFF
fun MAKELPARAM(x: Int, y: Int): Int = (x and 0xFFFF) or ((y and 0xFFFF) shl 16)

/**
 * Procedura di dialogo, equivalente di `BOOL FAR PASCAL Proc(HWND, WORD message, WORD wParam, LONG lParam)`.
 * E' `suspend` perche' al suo interno si possono chiamare [DialogBox] e [MessageBox] (bloccanti nell'originale).
 * Per WM_COMMAND `wParam` e' l'ID del controllo; per WM_SYSCOMMAND e' SC_CLOSE; per i controlli custom
 * (BMPView/BMPTipa) `lParam` contiene la posizione del click (LOWORD = x, HIWORD = y, in pixel logici).
 */
typealias DlgProc = suspend (hDlg: Dlg, message: Int, wParam: Int, lParam: Int) -> Boolean
