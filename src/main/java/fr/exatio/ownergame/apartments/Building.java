package fr.exatio.ownergame.apartments;

import java.util.List;

public class Building {

    private final int numOfBuilding;
    private String owner;
    private List<Integer> apartmentsInTheBuilding;
    private int price;
    private int income;
    public Building(int numOfBuilding, String owner, List<Integer> apartmentsInTheBuilding, int price, int income) {
        this.numOfBuilding = numOfBuilding;
        this.owner = owner;
        this.apartmentsInTheBuilding = apartmentsInTheBuilding;
        this.price = price;
        this.income = income;
    }

    public int getNumOfBuilding() {
        return numOfBuilding;
    }

    public String getOwner() {
        return owner;
    }

    public List<Integer> getApartmentsInTheBuilding() {
        return apartmentsInTheBuilding;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public int getPrice() {
        return this.price;
    }

    public int getIncome() {
        return this.income;
    }
}

