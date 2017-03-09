// Решение использует иерархию ресурсов: т.е. философ сначала берет вилку с меньшим номером, а затем с большим
// Возвращает же он сначала с большим номером, а затем с меньшим

public class DiningPhilosopherProblem {
    public static void main(String[] args) {
        int amountOfPhilosophers = 5;

        Fork[] forks = new Fork[amountOfPhilosophers];
        for (int i = 0; i < forks.length; i++) 
            forks[i] = new Fork(i);

        Philosopher[] philosophers = new Philosopher[amountOfPhilosophers];
        for(int i = 0; i < philosophers.length; i++) {
            philosophers[i] = new Philosopher("Philosopher-" + i, forks[i], forks[(i + 1) % amountOfPhilosophers]);
        }

        for(int i = 0;i < philosophers.length; i++){
            Thread thread = new Thread(philosophers[i]);
            thread.start();
        }
    }
}

class Fork {
    private boolean used;
    private final int id;

    public Fork(int id){
        this.id = id;
    }

    public int id() {
        return id;
    }

    public boolean used() {
        return used;
    }

    public synchronized void take() {
        System.out.println(this + " is being used");
        used = true;
    }
    public synchronized void release() {
        System.out.println(this + " is released now");
        used = false ;
    }

    @Override
    public String toString() {
        return "Fork-" + id;
    }
}

class Philosopher extends Thread
{
    private Fork firstFork;
    private Fork secondFork;
    private String name;
    private int turnsStarving;

    public Philosopher (String name, Fork left, Fork right) {
        if (left.id() < right.id()) {
            firstFork = left;
            secondFork = right;

        } else {
            firstFork = right;
            secondFork = left;
        }

        this.name = name;
        turnsStarving = 0;
    }
 
    public void eat()
    {
        if(!firstFork.used()) {
            firstFork.take();
            while (secondFork.used()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {}

                turnsStarving++;
                if (turnsStarving >= 10) {
                    System.out.println(name + " is starving!!!");
                    return;
                }
            }

            secondFork.take();

            System.out.println(name + " is eating");
            turnsStarving = 0;

            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {}

            secondFork.release();
            firstFork.release();
        }

        think();
    }

    public void think(){
        System.out.println(name + " is thinking");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {} 
    }

    public void run(){
        for(int i=0; i <= 25; i++){
            eat();
        }
    }
}