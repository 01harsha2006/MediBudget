package MediBudget;

import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicProgressBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Modern Medical-Fintech Design System.
 * Theme: Deep Navy (#0F172A) / Deep Slate (#1E293B) & Medical Teal (#0D9488) / Emerald (#10B981).
 * High-contrast, WCAG AA compliant, anti-aliased custom components.
 */
public class UITheme {

    // 1. Primary & Navigation Header Tokens
    public static final Color HEADER_BG = new Color(15, 23, 42);         // #0F172A Deep Navy
    public static final Color HEADER_SURFACE = new Color(30, 41, 59);    // #1E293B Slate 800
    public static final Color HEADER_SUBTITLE = new Color(148, 163, 184); // #94A3B8 Slate 400
    public static final Color HEADER_BORDER = new Color(51, 65, 85);     // #334155

    // 2. Dashboard Background & Cards
    public static final Color BG_PAGE = new Color(248, 250, 252);        // #F8FAFC Soft Light Slate
    public static final Color CARD_BG = Color.WHITE;                     // #FFFFFF
    public static final Color BORDER_COLOR = new Color(226, 232, 240);   // #E2E8F0 Slate 200
    public static final Color TEXT_DARK = new Color(15, 23, 42);         // #0F172A Primary Dark Slate
    public static final Color TEXT_MUTED = new Color(100, 116, 139);     // #64748B Muted Slate
    public static final Color TEXT_LIGHT = Color.WHITE;

    // 3. Primary Medical Teal & Accent System
    public static final Color PRIMARY = new Color(13, 148, 136);         // #0D9488 Medical Teal 600
    public static final Color PRIMARY_HOVER = new Color(15, 118, 110);   // #0F766E Teal 700
    public static final Color PRIMARY_DARK = new Color(17, 94, 89);      // #115E59 Teal 800
    public static final Color PRIMARY_LIGHT = new Color(240, 253, 250);  // #F0FDFA Soft Teal Tint
    public static final Color PRIMARY_ACCENT = new Color(20, 184, 166);  // #14B8A6 Teal 500

    // 4. Metric Card Border Accent System
    public static final Color ACCENT_TOTAL_EXPENSES = new Color(51, 65, 85);   // #334155 Deep Slate
    public static final Color ACCENT_MONTHLY_SPEND = new Color(217, 119, 6);   // #D97706 Amber 600
    public static final Color ACCENT_REMAINING_BUDGET = new Color(5, 150, 105); // #059669 Medical Emerald
    public static final Color ACCENT_FAMILY_MEMBERS = new Color(8, 145, 178);  // #0891B2 Cyan / Teal
    public static final Color ACCENT_ACTIVE_TREATMENTS = new Color(79, 70, 229);// #4F46E5 Indigo
    public static final Color ACCENT_PENDING_CLAIMS = new Color(225, 29, 72);  // #E11D48 Rose / Alert

    // 5. Status & Alert Palettes
    public static final Color EMERALD_REMAINING = new Color(4, 120, 87); // #047857 Deep Emerald
    public static final Color SUCCESS = new Color(5, 150, 105);          // #059669 Emerald 600
    public static final Color SUCCESS_DARK = new Color(4, 120, 87);
    public static final Color SUCCESS_BG = new Color(240, 253, 244);     // #F0FDF4 Very Light Emerald
    public static final Color WARNING = new Color(217, 119, 6);          // #D97706 Amber 600
    public static final Color WARNING_BG = new Color(254, 243, 199);     // #FEF3C7
    public static final Color DANGER = new Color(225, 29, 72);           // #E11D48 Rose 600
    public static final Color DANGER_DARK = new Color(190, 18, 60);      // #BE123C
    public static final Color DANGER_BG = new Color(255, 241, 242);      // #FFF1F2

    // 6. Typography
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SECTION = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BODY_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.BOLD, 11);
    public static final Font FONT_KPI_VAL = new Font("Segoe UI", Font.BOLD, 23);

    private static final NumberFormat CURRENCY_FORMAT = new DecimalFormat("₹#,##0.00");

    public static String formatCurrency(double amount) {
        return CURRENCY_FORMAT.format(amount);
    }

    /**
     * Primary Button: Medical Teal with crisp white text.
     */
    public static JButton createPrimaryButton(String text) {
        return createCustomButton(text, PRIMARY, PRIMARY_HOVER, PRIMARY_DARK, Color.WHITE, null);
    }

    /**
     * Secondary Button: Very soft teal tint with Teal border and dark teal text.
     */
    public static JButton createSecondaryButton(String text) {
        return createCustomButton(text, PRIMARY_LIGHT, new Color(204, 251, 241), new Color(153, 246, 228),
                PRIMARY_DARK, PRIMARY);
    }

    /**
     * Danger Button: Rose Red with white text.
     */
    public static JButton createDangerButton(String text) {
        return createCustomButton(text, DANGER, new Color(244, 63, 94), DANGER_DARK, Color.WHITE, null);
    }

    /**
     * Success Button: Medical Emerald with white text.
     */
    public static JButton createSuccessButton(String text) {
        return createCustomButton(text, SUCCESS, new Color(16, 185, 129), SUCCESS_DARK, Color.WHITE, null);
    }

    /**
     * Navigation Header Action Button:
     * Semi-transparent white background (rgba(255,255,255,0.1)),
     * white border (rgba(255,255,255,0.25)), and crisp white text.
     */
    public static JButton createHeaderButton(String text) {
        return createCustomButton(text,
                new Color(255, 255, 255, 26),
                new Color(255, 255, 255, 48),
                new Color(255, 255, 255, 75),
                Color.WHITE,
                new Color(255, 255, 255, 64)
        );
    }

    /**
     * Neutral / Cancel Button: Light slate background with slate text.
     */
    public static JButton createNeutralButton(String text) {
        return createCustomButton(text,
                new Color(241, 245, 249),
                new Color(226, 232, 240),
                new Color(203, 213, 225),
                new Color(51, 65, 85),
                new Color(203, 213, 225)
        );
    }

    /**
     * Universal Custom Button Factory with smooth anti-aliasing and rounded corners.
     */
    public static JButton createCustomButton(String text, Color normalBg, Color hoverBg, Color pressedBg,
                                             Color textColor, Color borderColor) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color currentBg;
                if (getModel().isPressed()) {
                    currentBg = pressedBg;
                } else if (getModel().isRollover()) {
                    currentBg = hoverBg;
                } else {
                    currentBg = normalBg;
                }

                g2.setColor(currentBg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);

                if (borderColor != null) {
                    g2.setColor(borderColor);
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                }

                g2.dispose();
                super.paintComponent(g);
            }
        };

        btn.setUI(new BasicButtonUI());
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setFont(FONT_BODY_BOLD);
        btn.setForeground(textColor);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return btn;
    }

    public static JTextField createTextField() {
        JTextField tf = new JTextField();
        tf.setFont(FONT_BODY);
        tf.setForeground(TEXT_DARK);
        tf.setBackground(Color.WHITE);
        tf.setCaretColor(PRIMARY);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(7, 10, 7, 10)
        ));
        return tf;
    }

    public static JPasswordField createPasswordField() {
        JPasswordField pf = new JPasswordField();
        pf.setFont(FONT_BODY);
        pf.setForeground(TEXT_DARK);
        pf.setBackground(Color.WHITE);
        pf.setCaretColor(PRIMARY);
        pf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(7, 10, 7, 10)
        ));
        return pf;
    }

    public static <T> JComboBox<T> createComboBox(T[] items) {
        JComboBox<T> cb = new JComboBox<>(items);
        cb.setFont(FONT_BODY);
        cb.setBackground(Color.WHITE);
        cb.setForeground(TEXT_DARK);
        cb.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        return cb;
    }

    public static JPanel createCardPanel() {
        JPanel card = new JPanel();
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));
        return card;
    }

    /**
     * Header banner with Deep Navy (#0F172A) background and Slate 400 (#94A3B8) subtitle.
     */
    public static JPanel createHeaderBanner(String title, String subtitle, String userGreeting) {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HEADER_BG);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, HEADER_SURFACE),
                BorderFactory.createEmptyBorder(18, 24, 18, 24)
        ));

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setOpaque(false);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(Color.WHITE);

        JLabel lblSubtitle = new JLabel(subtitle);
        lblSubtitle.setFont(FONT_SUBTITLE);
        lblSubtitle.setForeground(HEADER_SUBTITLE);

        leftPanel.add(lblTitle);
        leftPanel.add(Box.createVerticalStrut(4));
        leftPanel.add(lblSubtitle);

        header.add(leftPanel, BorderLayout.WEST);

        if (userGreeting != null && !userGreeting.isEmpty()) {
            JLabel lblUser = new JLabel(userGreeting);
            lblUser.setFont(FONT_BODY_BOLD);
            lblUser.setForeground(Color.WHITE);
            lblUser.setHorizontalAlignment(SwingConstants.RIGHT);
            header.add(lblUser, BorderLayout.EAST);
        }

        return header;
    }

    /**
     * Table styling with Deep Slate (#1E293B) header, bold white text (#F8FAFC),
     * alternating white / #F8FAFC rows, and subtle #E2E8F0 borders.
     */
    public static void styleTable(JTable table) {
        table.setFont(FONT_BODY);
        table.setRowHeight(32);
        table.setGridColor(BORDER_COLOR);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setSelectionBackground(new Color(204, 251, 241)); // Light teal tint #CCFBF1
        table.setSelectionForeground(PRIMARY_DARK);

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BODY_BOLD);
        header.setBackground(HEADER_SURFACE);
        header.setForeground(new Color(248, 250, 252));
        header.setPreferredSize(new Dimension(header.getWidth(), 36));
        header.setOpaque(true);

        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setBackground(HEADER_SURFACE);
                c.setForeground(new Color(248, 250, 252));
                setFont(FONT_BODY_BOLD);
                setHorizontalAlignment(SwingConstants.LEFT);
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return c;
            }
        };
        header.setDefaultRenderer(headerRenderer);

        // Zebra rows with high contrast
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (isSelected) {
                    c.setBackground(new Color(204, 251, 241));
                    c.setForeground(PRIMARY_DARK);
                } else {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : BG_PAGE);
                    c.setForeground(TEXT_DARK);
                }
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return c;
            }
        });
    }

    /**
     * Custom Budget Progress Bar:
     * Track / Empty Background: Soft slate #E2E8F0 (replaces dark gray box).
     * Progress Fill: Gradient from #0D9488 (Teal) to #10B981 (Emerald).
     */
    public static JProgressBar createBudgetProgressBar() {
        JProgressBar pb = new JProgressBar(0, 100) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int width = getWidth();
                int height = getHeight();

                // 1. Soft slate track #E2E8F0
                g2.setColor(BORDER_COLOR);
                g2.fillRoundRect(0, 0, width, height, 10, 10);

                // 2. Gradient fill: #0D9488 (Teal) to #10B981 (Emerald)
                int val = getValue();
                int min = getMinimum();
                int max = getMaximum();
                double percent = (double) (val - min) / (max - min);
                percent = Math.max(0.0, Math.min(1.0, percent));
                int fillWidth = (int) (width * percent);

                if (fillWidth > 0) {
                    GradientPaint gradient = new GradientPaint(
                            0, 0, PRIMARY,                          // #0D9488 Teal
                            fillWidth, 0, new Color(16, 185, 129)   // #10B981 Emerald
                    );
                    g2.setPaint(gradient);
                    g2.fillRoundRect(0, 0, fillWidth, height, 10, 10);
                }

                // 3. Crisp text display
                if (isStringPainted() && getString() != null) {
                    g2.setFont(FONT_BODY_BOLD);
                    String str = getString();
                    FontMetrics fm = g2.getFontMetrics();
                    int strWidth = fm.stringWidth(str);
                    int strHeight = fm.getAscent();
                    int x = (width - strWidth) / 2;
                    int y = (height + strHeight) / 2 - 2;

                    g2.setColor(percent > 0.55 ? Color.WHITE : TEXT_DARK);
                    g2.drawString(str, x, y);
                }

                g2.dispose();
            }
        };

        pb.setUI(new BasicProgressBarUI());
        pb.setOpaque(false);
        pb.setBorder(BorderFactory.createEmptyBorder());
        pb.setStringPainted(true);
        pb.setPreferredSize(new Dimension(800, 26));
        return pb;
    }
}
