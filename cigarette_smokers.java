import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;
 
public class SmokingAgent extends Thread {
 
    public Semaphore semaphoreSmoked = new Semaphore(0);
    public Semaphore semaphoreIngredient = new Semaphore(0);
    public String disposedIngredients = new String();
    public CountDownLatch latch;
    public Semaphore semaphoreLatchStart = new Semaphore(0);
    public boolean finished = false;
 
    public void run() {
        Random random = new Random();
        int currentIngredients;
        for (int i = 0; i < 10; i++) {
            latch = new CountDownLatch(3);
            semaphoreLatchStart.release(3);
            try {
                latch.await();
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            currentIngredients = random.nextInt(3);
            if (currentIngredients == 0) {
                disposedIngredients = "Paper and Matches";
            }
            if (currentIngredients == 1) {
                disposedIngredients = "Tabacco and Matches";
            }
            if (currentIngredients == 2) {
                disposedIngredients = "Paper and Tabacco";
            }
            System.out.println("Disposed Ingredients: " + disposedIngredients);
            semaphoreIngredient.release(3);
            try {
                semaphoreSmoked.acquire(3);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        finished = true;
        semaphoreLatchStart.release(3);
        semaphoreIngredient.release(3);
    }
}
 
public class Smoker extends Thread {
 
    private SmokingAgent agent;
    private String ownIngredient;
    private String missingIngredients;
 
    public Smoker(SmokingAgent agent, String ingredient) {
        this.agent = agent;
        ownIngredient = ingredient;
        if (ownIngredient.equals("Tabacco")) {
            missingIngredients = "Paper and Matches";
        }
        if (ownIngredient.equals("Paper")) {
            missingIngredients = "Tabacco and Matches";
        }
        if (ownIngredient.equals("Matches")) {
            missingIngredients = "Paper and Tabacco";
        }
    }
 
    public void run() {
        while (true) {
            if (agent.finished) {
                return;
            }
            try {
                agent.semaphoreLatchStart.acquire();
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            synchronized (agent.latch) {
                agent.latch.countDown();
            }
            try {
                agent.semaphoreIngredient.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (agent.finished) {
                System.out.println("The smoking agent " +
                        "is no longer available.I'll give up smoking.");
                return;
            }
            if (agent.disposedIngredients.equals(missingIngredients)) {
                System.out.println("I owe " + ownIngredient + " and I received "
                        + agent.disposedIngredients + ", so now I can smoke.");
            }
            agent.semaphoreSmoked.release();
        }
    }
}
 
public class Smoking {
 
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        SmokingAgent agent = new SmokingAgent();
        Smoker tabaccoSmoker = new Smoker(agent, "Tabacco");
        Smoker paperSmoker = new Smoker(agent, "Paper");
        Smoker matchesSmoker = new Smoker(agent, "Matches");
        agent.start();
        tabaccoSmoker.start();
        paperSmoker.start();
        matchesSmoker.start();
    }
}