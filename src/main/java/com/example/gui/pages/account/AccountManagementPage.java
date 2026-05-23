package com.example.gui.pages.account;

import com.example.dto.AccountDTO;
import com.example.gui.components.OverlayUtil;

import javax.swing.*;
import java.awt.*;

public class AccountManagementPage extends JPanel {
    private AccountListPage listPage;

    public AccountManagementPage(AccountDTO loggedInAccount) {
        setLayout(new BorderLayout());
        setOpaque(false);

        listPage = new AccountListPage(this);
        add(listPage, BorderLayout.CENTER);
    }

    private JFrame getOwnerFrame() {
        Window window = SwingUtilities.getWindowAncestor(this);
        return (window instanceof JFrame) ? (JFrame) window : null;
    }

    public void showList() {
        listPage.refresh();
    }

    public void showDetail(AccountDTO account) {
        JFrame owner = getOwnerFrame();
        if (owner == null || account == null) {
            return;
        }

        OverlayUtil.hideOverlay(owner);
        AccountDetailPage detailPage = new AccountDetailPage(this, account, () -> OverlayUtil.hideOverlay(owner));
        OverlayUtil.showOverlay(owner, "Chi tiết người dùng", detailPage, new Dimension(720, 430));
    }

    public void showCreate() {
        JFrame owner = getOwnerFrame();
        if (owner == null) return;

        OverlayUtil.hideOverlay(owner);
        AccountCreatePage createPage = new AccountCreatePage(() -> {
            OverlayUtil.hideOverlay(owner);
            showList();
        });

        OverlayUtil.showOverlay(owner, "Create New User", createPage, new Dimension(720, 500));
    }

    public void showEdit(AccountDTO account) {
        JFrame owner = getOwnerFrame();
        if (owner == null) return;

        OverlayUtil.hideOverlay(owner);
        AccountEditPage editPage = new AccountEditPage(account, () -> {
            OverlayUtil.hideOverlay(owner);
            showList();
        });

        OverlayUtil.showOverlay(owner, "Edit User Information", editPage, new Dimension(720, 430));
    }

    public void showChangePassword(AccountDTO account) {
        JFrame owner = getOwnerFrame();
        if (owner == null || account == null) {
            return;
        }

        OverlayUtil.hideOverlay(owner);
        AccountChangePasswordPage changePasswordPage = new AccountChangePasswordPage(account, () -> {
            OverlayUtil.hideOverlay(owner);
            showList();
        });

        OverlayUtil.showOverlay(owner, "Change Password", changePasswordPage, new Dimension(560, 320));
    }
}