package com.mycompany.maandeeqresturent;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class Maandeeqresturent extends JFrame {

    Connection con;
    int loggedUserId = 0;

    // Signup
    JTextField txtFull, txtEmail, txtUser;
    JPasswordField txtPass;

    // Login
    JTextField txtLoginUser;
    JPasswordField txtLoginPass;

    // Dashboard
    JComboBox<String> foodBox, drinkBox;
    JTextField foodQty, drinkQty;
    JTextArea receipt;

    public Maandeeqresturent() {
        connectDB();
        signupPage();   // MARKA PROGRAM-KU FURMO → SIGNUP
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    // ================= DATABASE =================
    void connectDB() {
        try {
            con = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/aflaxbar",
                "root",
                "abdisamad77@"
            );
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e);
        }
    }

    // ================= SIGNUP PAGE =================
    void signupPage() {
        getContentPane().removeAll();
        setTitle("Maandeeq Restaurant - Signup");
        setLayout(new GridLayout(5, 2, 5, 5));
getContentPane().setBackground(Color.YELLOW);

        txtFull = new JTextField();
        txtEmail = new JTextField();
        txtUser = new JTextField();
        txtPass = new JPasswordField();

        JButton btnSignup = new JButton("Sign Up");
        JButton btnLogin = new JButton("Go to Login");
        
        
        btnSignup.setBackground(Color.black);
btnSignup.setForeground(Color.green);

btnLogin.setBackground(Color.BLUE);
btnLogin.setForeground(Color.WHITE);


        add(new JLabel("Full Name"));
        add(txtFull);
        add(new JLabel("Email"));
        add(txtEmail);
        add(new JLabel("Username"));
        add(txtUser);
        add(new JLabel("Password"));
        add(txtPass);
        add(btnSignup);
        add(btnLogin);

        btnSignup.addActionListener(e -> signup());
        btnLogin.addActionListener(e -> loginPage());

        setSize(400, 300);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    void signup() {
        try {
            String sql =
                "INSERT INTO users(full_name,email,username,password) VALUES (?,?,?,?)";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, txtFull.getText());
            ps.setString(2, txtEmail.getText());
            ps.setString(3, txtUser.getText());
            ps.setString(4, txtPass.getText());
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Account Created Successfully");
            loginPage();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Username or Email already exists");
        }
    }

    // ================= LOGIN PAGE =================
    void loginPage() {
        getContentPane().removeAll();
        setTitle("Maandeeq Restaurant - Login");
        setLayout(new GridLayout(3, 2, 5, 5));
        
getContentPane().setBackground(Color.black); 

        txtLoginUser = new JTextField();
        txtLoginPass = new JPasswordField();
        JButton btnLogin = new JButton("Login");
        
btnLogin.setBackground(Color.green);
btnLogin.setForeground(Color.green);


        add(new JLabel("Username"));
        add(txtLoginUser);
        add(new JLabel("Password"));
        add(txtLoginPass);
        add(btnLogin);

        btnLogin.addActionListener(e -> login());

        setSize(350, 200);
        setLocationRelativeTo(null);
        revalidate();
        repaint();
    }

    void login() {
        try {
            String sql =
                "SELECT * FROM users WHERE username=? AND password=?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, txtLoginUser.getText());
            ps.setString(2, txtLoginPass.getText());

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                loggedUserId = rs.getInt("user_id");
                dashboard();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid Login");
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e);
        }
    }

    // ================= DASHBOARD =================
    void dashboard() {
        getContentPane().removeAll();
        setTitle("Maandeeq Restaurant - Order System");
        setLayout(null);
        getContentPane().setBackground(Color.YELLOW);

        JLabel lblFood = new JLabel("Select Food:");
        lblFood.setBounds(30, 30, 120, 25);

        foodBox = new JComboBox<>();
        foodBox.setBounds(150, 30, 200, 25);

        JLabel lblFoodQty = new JLabel("Qty:");
        lblFoodQty.setBounds(370, 30, 40, 25);

        foodQty = new JTextField("1");
        foodQty.setBounds(410, 30, 50, 25);

        JLabel lblDrink = new JLabel("Select Drink:");
        lblDrink.setBounds(30, 70, 120, 25);

        drinkBox = new JComboBox<>();
        drinkBox.setBounds(150, 70, 200, 25);

        JLabel lblDrinkQty = new JLabel("Qty:");
        lblDrinkQty.setBounds(370, 70, 40, 25);

        drinkQty = new JTextField("1");
        drinkQty.setBounds(410, 70, 50, 25);

        receipt = new JTextArea();
        receipt.setEditable(false);
        JScrollPane sp = new JScrollPane(receipt);
        sp.setBounds(30, 120, 430, 200);

        JButton btnCompute = new JButton("COMPUTE TOTAL");
        btnCompute.setBounds(160, 340, 180, 40);

        add(lblFood);
        add(foodBox);
        add(lblFoodQty);
        add(foodQty);
        add(lblDrink);
        add(drinkBox);
        add(lblDrinkQty);
        add(drinkQty);
        add(sp);
        add(btnCompute);

        loadMenu();

        btnCompute.addActionListener(e -> computeOrder());

        setSize(520, 440);
        setLocationRelativeTo(null);
        revalidate();
        repaint();
    }

    void loadMenu() {
        try {
            Statement st = con.createStatement();

            ResultSet rs1 = st.executeQuery("SELECT food_name FROM food_menu");
            while (rs1.next()) {
                foodBox.addItem(rs1.getString(1));
            }

            ResultSet rs2 = st.executeQuery("SELECT drink_name FROM drink_menu");
            while (rs2.next()) {
                drinkBox.addItem(rs2.getString(1));
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e);
        }
    }

    void computeOrder() {
        try {
            String food = foodBox.getSelectedItem().toString();
            String drink = drinkBox.getSelectedItem().toString();

            int fq = Integer.parseInt(foodQty.getText());
            int dq = Integer.parseInt(drinkQty.getText());

            int foodPrice = getPrice("food_menu", "food_name", food);
            int drinkPrice = getPrice("drink_menu", "drink_name", drink);

            int foodTotal = foodPrice * fq;
            int drinkTotal = drinkPrice * dq;
            int grandTotal = foodTotal + drinkTotal;

            receipt.setText(
                "----- MAANDEEQ RESTAURANT RECEIPT -----\n\n" +
                "Food: " + food + "\n" +
                "Quantity: " + fq + "\n" +
                "Food Cost: " + foodTotal + " KES\n\n" +
                "Drink: " + drink + "\n" +
                "Quantity: " + dq + "\n" +
                "Drink Cost: " + drinkTotal + " KES\n\n" +
                "-------------------------------------\n" +
                "GRAND TOTAL: " + grandTotal + " KES"
            );

            String sql =
                "INSERT INTO orders(user_id,food_id,drink_id,food_qty,drink_qty,total_cost) " +
                "VALUES(?,?,?,?,?,?)";

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, loggedUserId);
            ps.setInt(2, getId("food_menu", "food_name", food));
            ps.setInt(3, getId("drink_menu", "drink_name", drink));
            ps.setInt(4, fq);
            ps.setInt(5, dq);
            ps.setInt(6, grandTotal);
            ps.executeUpdate();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e);
        }
    }

    int getPrice(String table, String col, String val) throws Exception {
        Statement st = con.createStatement();
        ResultSet rs =
            st.executeQuery("SELECT price FROM " + table + " WHERE " + col + "='" + val + "'");
        rs.next();
        return rs.getInt("price");
    }

    int getId(String table, String col, String val) throws Exception {
        Statement st = con.createStatement();
        ResultSet rs =
            st.executeQuery("SELECT * FROM " + table + " WHERE " + col + "='" + val + "'");
        rs.next();
        return rs.getInt(1);
    }

    public static void main(String[] args) {
        new Maandeeqresturent();
    }
}
