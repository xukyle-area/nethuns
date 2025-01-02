package com.gantenx.nethuns.converter;

import java.util.List;

public class OrderListResponse {

    private List<OrderList> orderLists;

    // Getters and Setters
    public List<OrderList> getOrderLists() {
        return orderLists;
    }

    public void setOrderLists(List<OrderList> orderLists) {
        this.orderLists = orderLists;
    }

    // OrderList class
    public static class OrderList {
        private long orderListId;
        private String contingencyType;
        private String listStatusType;
        private String listOrderStatus;
        private String listClientOrderId;
        private long transactionTime;
        private String symbol;
        private List<Order> orders;

        // Getters and Setters
        public long getOrderListId() {
            return orderListId;
        }

        public void setOrderListId(long orderListId) {
            this.orderListId = orderListId;
        }

        public String getContingencyType() {
            return contingencyType;
        }

        public void setContingencyType(String contingencyType) {
            this.contingencyType = contingencyType;
        }

        public String getListStatusType() {
            return listStatusType;
        }

        public void setListStatusType(String listStatusType) {
            this.listStatusType = listStatusType;
        }

        public String getListOrderStatus() {
            return listOrderStatus;
        }

        public void setListOrderStatus(String listOrderStatus) {
            this.listOrderStatus = listOrderStatus;
        }

        public String getListClientOrderId() {
            return listClientOrderId;
        }

        public void setListClientOrderId(String listClientOrderId) {
            this.listClientOrderId = listClientOrderId;
        }

        public long getTransactionTime() {
            return transactionTime;
        }

        public void setTransactionTime(long transactionTime) {
            this.transactionTime = transactionTime;
        }

        public String getSymbol() {
            return symbol;
        }

        public void setSymbol(String symbol) {
            this.symbol = symbol;
        }

        public List<Order> getOrders() {
            return orders;
        }

        public void setOrders(List<Order> orders) {
            this.orders = orders;
        }

        // Order class
        public static class Order {
            private String symbol;
            private long orderId;
            private String clientOrderId;

            // Getters and Setters
            public String getSymbol() {
                return symbol;
            }

            public void setSymbol(String symbol) {
                this.symbol = symbol;
            }

            public long getOrderId() {
                return orderId;
            }

            public void setOrderId(long orderId) {
                this.orderId = orderId;
            }

            public String getClientOrderId() {
                return clientOrderId;
            }

            public void setClientOrderId(String clientOrderId) {
                this.clientOrderId = clientOrderId;
            }
        }
    }
}
