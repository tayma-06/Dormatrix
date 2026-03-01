package controllers.food;

import java.time.LocalDate;
import java.util.concurrent.*;
import models.food.MealType;

public class TokenPurchaseService {
    private static TokenPurchaseService instance;
    private final ExecutorService executor;
    private final BlockingQueue<TokenRequest> requestQueue;
    private final CafeteriaController cafeteriaController;
    private volatile boolean running;

    private TokenPurchaseService() {
        this.executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "TokenPurchaseWorker");
            t.setDaemon(true);
            return t;
        });
        this.requestQueue = new LinkedBlockingQueue<>();
        this.cafeteriaController = new CafeteriaController();
        this.running = false;
    }

    public static synchronized TokenPurchaseService getInstance() {
        if (instance == null) {
            instance = new TokenPurchaseService();
        }
        return instance;
    }

    public void start() {
        if (running) {
            return;
        }
        running = true;

        executor.submit(() -> {
            System.out.println("[TokenPurchaseService] Worker thread started");
            while (running || !requestQueue.isEmpty()) {
                try {
                    TokenRequest request = requestQueue.poll(100, TimeUnit.MILLISECONDS);
                    if (request != null) {
                        processRequest(request);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            System.out.println("[TokenPurchaseService] Worker thread stopped");
        });
    }

    private void processRequest(TokenRequest request) {
        try {
            String result;
            if (request.specificDate != null && request.mealType != null) {
                result = cafeteriaController.purchaseTokenForDay(
                    request.username, 
                    request.specificDate, 
                    request.mealType
                );
            } else {
                result = cafeteriaController.purchaseToken(request.username);
            }
            request.future.complete(result);
        } catch (Exception e) {
            request.future.completeExceptionally(e);
        }
    }

    public CompletableFuture<String> purchaseTokenAsync(String username) {
        if (!running) {
            return CompletableFuture.completedFuture(
                "Error: Token purchase service not started"
            );
        }

        CompletableFuture<String> future = new CompletableFuture<>();
        TokenRequest request = new TokenRequest(username, future);
        
        if (!requestQueue.offer(request)) {
            future.complete("Error: Unable to queue purchase request");
        }
        
        return future;
    }

    public CompletableFuture<String> purchaseTokenForDayAsync(
            String username, LocalDate date, MealType mealType) {
        if (!running) {
            return CompletableFuture.completedFuture(
                "Error: Token purchase service not started"
            );
        }

        CompletableFuture<String> future = new CompletableFuture<>();
        TokenRequest request = new TokenRequest(username, date, mealType, future);
        
        if (!requestQueue.offer(request)) {
            future.complete("Error: Unable to queue purchase request");
        }
        
        return future;
    }

    public String purchaseToken(String username) {
        try {
            return purchaseTokenAsync(username).get(5, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            return "Error: Purchase request timed out";
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "Error: Purchase interrupted";
        } catch (ExecutionException e) {
            return "Error: " + e.getCause().getMessage();
        }
    }

    public String purchaseTokenForDay(String username, LocalDate date, MealType mealType) {
        try {
            return purchaseTokenForDayAsync(username, date, mealType)
                .get(5, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            return "Error: Purchase request timed out";
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "Error: Purchase interrupted";
        } catch (ExecutionException e) {
            return "Error: " + e.getCause().getMessage();
        }
    }

    public int getQueueSize() {
        return requestQueue.size();
    }

    public void shutdown() {
        running = false;
        executor.shutdown();
        try {
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
            System.out.println("[TokenPurchaseService] Shutdown complete");
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private static class TokenRequest {
        final String username;
        final LocalDate specificDate;
        final MealType mealType;
        final CompletableFuture<String> future;

        TokenRequest(String username, CompletableFuture<String> future) {
            this.username = username;
            this.specificDate = null;
            this.mealType = null;
            this.future = future;
        }

        TokenRequest(String username, LocalDate date, MealType mealType, 
                    CompletableFuture<String> future) {
            this.username = username;
            this.specificDate = date;
            this.mealType = mealType;
            this.future = future;
        }
    }
}
