package com.rocket.biometrix.Login;

/**
 * Created by TJ on 5/5/2016. Helper class for SettingsAndEntryHelper.. Basically just a pair with 4 elements
 */
public class Quartet<T1, T2, T3, T4>
{
    public T1 first;
    public T2 second;
    public T3 third;
    public T4 fourth;

    public Quartet(T1 first, T2 second, T3 third, T4 fourth) {
        this.third = third;
        this.first = first;
        this.second = second;
        this.fourth = fourth;
    }
}