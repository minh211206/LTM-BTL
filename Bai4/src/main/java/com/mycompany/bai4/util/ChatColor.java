package com.mycompany.bai4.util;

import java.awt.Color;
import java.awt.Font;


public class ChatColor {

    // Nền
    public static final Color BG_PAGE   = new Color(235, 237, 240);
    public static final Color BG_PANEL  = new Color(255, 255, 255);
    public static final Color BG_INPUT  = new Color(240, 242, 245);

    // Màu nhấn
    public static final Color ACCENT     = new Color(0,   132, 255);
    public static final Color ACCENT2    = new Color(49,  162, 76);
    public static final Color DANGER     = new Color(255, 59,  48);
    public static final Color NAME_OTHER = new Color(101, 103, 107);

    // Chữ 
    public static final Color TEXT_MAIN = new Color(28,  30,  33);
    public static final Color TEXT_DIM  = new Color(134, 138, 143);

    // Bubble tin nhắn / file card
    public static final Color BUBBLE_SELF        = new Color(0,   132, 255); // bubble của mình – xanh
    public static final Color BUBBLE_OTHER       = Color.WHITE;              // bubble người khác – trắng, nổi bật trên nền xám
    public static final Color BUBBLE_OTHER_BORDER = new Color(222, 224, 228); // viền nhẹ cho bubble trắng
    public static final Color BUBBLE_SELF_TEXT   = Color.WHITE;
    public static final Color BUBBLE_OTHER_TEXT  = new Color(28, 30, 33);

    // Huy hiệu số tin nhắn chưa đọc
    public static final Color BADGE_BG   = new Color(255, 59, 48);
    public static final Color BADGE_TEXT = Color.WHITE;

    // Sidebar
    public static final Color SIDEBAR_SELECTED = new Color(231, 243, 255);
    public static final Color SIDEBAR_GROUP_TINT = new Color(244, 249, 255);
    public static final Color DIVIDER = new Color(224, 226, 230);

    //Font chữ 
    public static final Font FONT_MAIN  = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_BOLD  = new Font("Segoe UI", Font.BOLD,  14);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 12);

    private ChatColor() {} // chỉ chứa hằng số, không cần khởi tạo
}