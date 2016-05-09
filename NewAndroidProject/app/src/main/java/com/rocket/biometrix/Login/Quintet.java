package com.rocket.biometrix.Login;

/**
 * Created by TJ on 5/5/2016. Helper class for SettingsAndEntryHelper.. Basically just a pair with 5 elements
 */
public class Quintet<T1, T2, T3, T4, T5>
{
    public T1 first;
    public T2 second;
    public T3 third;
    public T4 fourth;
    public T5 fifth;

    public Quintet(T1 first, T2 second, T3 third, T4 fourth, T5 fifth) {
        this.third = third;
        this.first = first;
        this.second = second;
        this.fourth = fourth;
        this.fifth = fifth;
    }
}