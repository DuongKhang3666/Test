package com.example.gui;

import com.example.dto.AccountDTO;
import com.example.gui.components.AppTitle;
import com.example.gui.components.Sidebar;
import com.example.gui.pages.account.AccountManagementPage;
import com.example.gui.pages.candidate.CandidateManagement;
import com.example.gui.pages.candidate.CandidateStatsPage;
import com.example.gui.pages.bonusscore.BonusScoreManagement;
import com.example.gui.pages.conversion.ConversionTableManagementPage;
import com.example.gui.pages.majorcombination.MajorGroupManagementPage;
import com.example.gui.pages.score.ScoreManagement;
import com.example.gui.pages.aspiration.AspirationManagement;
import com.example.gui.pages.score.ScoreManagement;
import com.example.gui.pages.admissionprocess.AdmissionProcessManagementPage;
import javax.swing.*;
import java.awt.*;

public class MainLayout extends JFrame {
    private final CardLayout cardLayout;
    private final JPanel contentPanel;

    public MainLayout(AccountDTO loggedInAccount) {
        setSize(1280, 720);
        setLocationRelativeTo(null);
        
        setUndecorated(true); 
        setLayout(new BorderLayout());

        // Thanh tiêu đề AppTitle
        AppTitle appTitle = new AppTitle(this);
        add(appTitle, BorderLayout.NORTH);

        // CardLayout ở giữa
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(Color.decode("#F8F9FA"));

        // Menu Sidebar
        Sidebar sideBar = new Sidebar(loggedInAccount, this::switchPage);
        add(sideBar, BorderLayout.WEST);

        //User Management
        JPanel userManagementPage = new AccountManagementPage(loggedInAccount);
        userManagementPage.setName("User Management"); // Tên này phải trùng với Sidebar
        contentPanel.add(userManagementPage, "User Management");

        //Candidate Management
        JPanel candidateManagementPage = new CandidateManagement();
        candidateManagementPage.setName("Candidates");
        contentPanel.add(candidateManagementPage, "Candidates");

        //Aspiration Management (Quản lý nguyện vọng)
        JPanel aspirationManagementPage = new AspirationManagement(); 
        aspirationManagementPage.setName("Aspirations");             
        contentPanel.add(aspirationManagementPage, "Aspirations"); 
         
        // Candidate Statistics
        JPanel candidateStatsPage = new CandidateStatsPage();
        candidateStatsPage.setName("Candidate Stats");
        contentPanel.add(candidateStatsPage, "Candidate Stats");

        //Bonus Score Management
        JPanel bonusScoreManagementPage = new BonusScoreManagement();
        bonusScoreManagementPage.setName("Bonus Scores");
        contentPanel.add(bonusScoreManagementPage, "Bonus Scores");

        //Major & Combination Management
        JPanel majorComboManagementPage = new MajorGroupManagementPage();
        majorComboManagementPage.setName("Majors & Combinations");
        contentPanel.add(majorComboManagementPage, "Majors & Combinations");
        
        //Score Management
        JPanel scoreManagementPage = new ScoreManagement(); 
        scoreManagementPage.setName("Score Management");
        contentPanel.add(scoreManagementPage, "Score Management");

        //Conversion Tables
        JPanel conversionTablePage = new ConversionTableManagementPage();
        conversionTablePage.setName("Conversion Tables");
        contentPanel.add(conversionTablePage, "Conversion Tables");

        JPanel admissionProcessPage = new AdmissionProcessManagementPage();
        admissionProcessPage.setName("Admission Process");
        contentPanel.add(admissionProcessPage, "Admission Process");

        add(contentPanel, BorderLayout.CENTER);

        // Hiển thị trang mặc định khi vừa đăng nhập xong
        cardLayout.show(contentPanel, "User Management");
        sideBar.setSelectedMenu("User Management");
    }

    private void switchPage(String pageName) {
        if (hasPage(pageName)) {
            cardLayout.show(contentPanel, pageName);
            contentPanel.revalidate();
            contentPanel.repaint();
            return;
        }

        JOptionPane.showMessageDialog(
                this,
                "Trang '" + pageName + "' đang được phát triển.",
                "Thông báo",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private boolean hasPage(String pageName) {
        for (Component c : contentPanel.getComponents()) {
            if (pageName.equals(c.getName())) {
                return true;
            }
        }
        // CẬP NHẬT CHUỖI KHỚP FALLBACK
        return "User Management".equals(pageName) 
                || "Conversion Tables".equals(pageName) 
                || "Score Management".equals(pageName)
                || "Aspirations".equals(pageName)
                || "Admission Process".equals(pageName); // THÊM DÒNG NÀY
    }
}