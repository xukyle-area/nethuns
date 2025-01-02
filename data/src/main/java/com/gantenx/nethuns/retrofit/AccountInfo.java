package com.gantenx.nethuns.retrofit;

import java.util.List;

public class AccountInfo {

    private int makerCommission;
    private int takerCommission;
    private int buyerCommission;
    private int sellerCommission;
    private CommissionRates commissionRates;
    private boolean canTrade;
    private boolean canWithdraw;
    private boolean canDeposit;
    private boolean brokered;
    private boolean requireSelfTradePrevention;
    private boolean preventSor;
    private long updateTime;
    private String accountType;
    private List<Balance> balances;
    private List<String> permissions;
    private long uid;

    // Getters and Setters

    public int getMakerCommission() {
        return makerCommission;
    }

    public void setMakerCommission(int makerCommission) {
        this.makerCommission = makerCommission;
    }

    public int getTakerCommission() {
        return takerCommission;
    }

    public void setTakerCommission(int takerCommission) {
        this.takerCommission = takerCommission;
    }

    public int getBuyerCommission() {
        return buyerCommission;
    }

    public void setBuyerCommission(int buyerCommission) {
        this.buyerCommission = buyerCommission;
    }

    public int getSellerCommission() {
        return sellerCommission;
    }

    public void setSellerCommission(int sellerCommission) {
        this.sellerCommission = sellerCommission;
    }

    public CommissionRates getCommissionRates() {
        return commissionRates;
    }

    public void setCommissionRates(CommissionRates commissionRates) {
        this.commissionRates = commissionRates;
    }

    public boolean isCanTrade() {
        return canTrade;
    }

    public void setCanTrade(boolean canTrade) {
        this.canTrade = canTrade;
    }

    public boolean isCanWithdraw() {
        return canWithdraw;
    }

    public void setCanWithdraw(boolean canWithdraw) {
        this.canWithdraw = canWithdraw;
    }

    public boolean isCanDeposit() {
        return canDeposit;
    }

    public void setCanDeposit(boolean canDeposit) {
        this.canDeposit = canDeposit;
    }

    public boolean isBrokered() {
        return brokered;
    }

    public void setBrokered(boolean brokered) {
        this.brokered = brokered;
    }

    public boolean isRequireSelfTradePrevention() {
        return requireSelfTradePrevention;
    }

    public void setRequireSelfTradePrevention(boolean requireSelfTradePrevention) {
        this.requireSelfTradePrevention = requireSelfTradePrevention;
    }

    public boolean isPreventSor() {
        return preventSor;
    }

    public void setPreventSor(boolean preventSor) {
        this.preventSor = preventSor;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public List<Balance> getBalances() {
        return balances;
    }

    public void setBalances(List<Balance> balances) {
        this.balances = balances;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    // Inner classes for CommissionRates and Balance

    public static class CommissionRates {
        private String maker;
        private String taker;
        private String buyer;
        private String seller;

        // Getters and Setters

        public String getMaker() {
            return maker;
        }

        public void setMaker(String maker) {
            this.maker = maker;
        }

        public String getTaker() {
            return taker;
        }

        public void setTaker(String taker) {
            this.taker = taker;
        }

        public String getBuyer() {
            return buyer;
        }

        public void setBuyer(String buyer) {
            this.buyer = buyer;
        }

        public String getSeller() {
            return seller;
        }

        public void setSeller(String seller) {
            this.seller = seller;
        }
    }

    public static class Balance {
        private String asset;
        private String free;
        private String locked;

        // Getters and Setters

        public String getAsset() {
            return asset;
        }

        public void setAsset(String asset) {
            this.asset = asset;
        }

        public String getFree() {
            return free;
        }

        public void setFree(String free) {
            this.free = free;
        }

        public String getLocked() {
            return locked;
        }

        public void setLocked(String locked) {
            this.locked = locked;
        }
    }
}
