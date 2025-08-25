package com.expensemanager.service;

import com.expensemanager.dao.*;
import com.expensemanager.model.*;
import com.expensemanager.util.DateUtils;
import com.expensemanager.util.CurrencyUtils;
import java.util.*;

public class SavingService {
    
    private final DAOFactory daoFactory;
    private final SavingDAO savingDAO;
    private final SavingTransactionDAO savingTransactionDAO;
    private final UserDAO userDAO;
    
    public SavingService() {
        this.daoFactory = DAOFactory.getInstance();
        this.savingDAO = daoFactory.getSavingDAO();
        this.savingTransactionDAO = daoFactory.getSavingTransactionDAO();
        this.userDAO = daoFactory.getUserDAO();
    }