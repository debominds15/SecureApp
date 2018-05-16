package com.bdebo.secureapp.model;

/**
 * This model class is used to hold the details of the user registered
 */
public class User {

    /**
     * Holds the id of the user
     */
    private int id;
    /**
     * Holds the unique code of the user
     */
    private int uniqueCode;
    /**
     * Holds the name of the user
     */
    private String name;
    /**
     * Holds the password of the SecureApp
     */
    private String pass;
    /**
     * Holds the username of the SecureApp
     */
    private String username;

    /**
     * Holds the first security question of the SecureApp
     */
    private String sec_q1;
    /**
     * Holds the first security answer of the SecureApp
     */
    private String sec_ans1;
    /**
     * Holds the second security question of the SecureApp
     */
    private String sec_q2;
    /**
     * Holds the second security answer of the SecureApp
     */
    private String sec_ans2;

    /**
     * Holds the id of the user
     * @return id
     */
    public int getId() {
        return id;
    }
    /**
     * Set the id of the user
     */
    public void setId(int id) {
        this.id = id;
    }
    /**
     * Holds the name of the user
     * @return name
     */
    public String getName() {
        return name;
    }
    /**
     * Set the name of the user
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Holds the username of the SecureApp
     * @return username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Holds the unique code of the user
     * @return unique code
     */
    public int getUniqueCode() {
        return uniqueCode;
    }

    /**
     * Set the unique code of the user
     */
    public void setUniqueCode(int uniqueCode) {
        this.uniqueCode = uniqueCode;
    }

    public User(int id, String name, String username, String pass, String sec_q1, String sec_ans1,String sec_q2, String sec_ans2, int uniqueCode) {
        this.id = id;
        this.name = name;
        this.pass = pass;
        this.username = username;
        this.sec_q1 = sec_q1;
        this.sec_ans1 = sec_ans1;
        this.sec_q2 = sec_q2;
        this.sec_ans2 = sec_ans2;
        this.uniqueCode=uniqueCode;
    }

    /**
     * Holds the password of the SecureApp
     * @return password
     */
    public String getPass() {
        return pass;
    }

    /**
     * Set the password of the SecureApp
     */
    public void setPass(String pass) {
        this.pass = pass;
    }

    /**
     * Set the username of the SecureApp
     */
    public void setUsername(String username) {
        this.username = username;
    }
    /**
     * Holds the first security question of the SecureApp
     * @return first security question
     */
    public String getSec_q1() {
        return sec_q1;
    }
    /**
     * Set the first security question of the SecureApp
     */
    public void setSec_q1(String sec_q) {
        this.sec_q1 = sec_q;
    }
    /**
     * Holds the first security answer of the SecureApp
     * @return first security answer
     */
    public String getSec_ans1() {
        return sec_ans1;
    }

    /**
     * Holds the second security question of the SecureApp
     * @return second security question
     */
    public String getSec_q2() {
        return sec_q2;
    }
    /**
     * Set the second security question of the SecureApp
     */
    public void setSec_q2(String sec_q) {
        this.sec_q2 = sec_q;
    }
    /**
     * Holds the second security answer of the SecureApp
     * @return second security answer
     */
    public String getSec_ans2() {
        return sec_ans2;
    }
    public User() {
        super();
    }

    /**
     * Holds the first security answer of the SecureApp
     */
    public void setSec_ans1(String sec_ans) {
        this.sec_ans1 = sec_ans;
    }
    /**
     * Holds the second security answer of the SecureApp
     */
    public void setSec_ans2(String sec_ans) {
        this.sec_ans2 = sec_ans;
    }

}
