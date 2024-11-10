package store.domain.promotion;

import store.domain.Product;
import store.domain.Promotion;
import store.domain.inventory.InventoryManager;
import store.enums.constants.AnswerConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PromotionProcessor {
    private static final int NO_ANY_PRODUCT = 0;
    private static final int GET_ONE_FREE = 1;
    private static final int NO_ANY_PROMOTION_BOON = 0;
    private static final int ONE_PROMOTION_BOON = 1;

    private final InventoryManager inventoryManager;
    private final UserInteractionHandler userInteractionHandler;
    private final List<Promotion> promotions;

    public PromotionProcessor(InventoryManager inventoryManager, UserInteractionHandler userInteractionHandler, List<Promotion> promotions) {
        this.inventoryManager = inventoryManager;
        this.userInteractionHandler = userInteractionHandler;
        this.promotions = promotions;
    }

    public List<Integer> buyPromotionProduct(String productName, int purchaseQuantity) {
        List<Integer> resultList = new ArrayList<>();
        Product productWithPromotion = getProductWithPromotion(productName);
        int promotionBoon = getPromotionBoon(productWithPromotion);
        int result = processPurchase(productName, purchaseQuantity, productWithPromotion, promotionBoon);
        int lackQuantity = NO_ANY_PRODUCT;
        resultList.add(result);
        resultList.add(lackQuantity);
        return resultList;
    }

    private Product getProductWithPromotion(String productName) {
        Optional<Product> productOpt = inventoryManager.findProductWithPromotion(productName);
        return productOpt.get();
    }

    private int processPurchase(String productName, int purchaseQuantity, Product productWithPromotion, int promotionBoon) {
        if (productWithPromotion.isSmallQuantityThanPromotionBoon(promotionBoon)) {
            return handleSmallQuantityCase(productName, purchaseQuantity, productWithPromotion);
        }
        return processPurchaseQuantity(productName, purchaseQuantity, productWithPromotion, promotionBoon);
    }

    private int getPromotionBoon(Product product) {
        String promotionName = product.getNameOfPromotion();
        Optional<Promotion> promotionOpt = promotions.stream()
                .filter(promotion -> promotion.isSamePromotionName(promotionName))
                .findFirst();
        Promotion promotion = promotionOpt.get();
        return promotion.getPromotionBoon();
    }

    private int handleSmallQuantityCase(String productName, int purchaseQuantity, Product productWithPromotion) {
        int currentQuantity = productWithPromotion.firstReduceQuantityThanCheck(purchaseQuantity);
        if (currentQuantity >= NO_ANY_PRODUCT) {
            return ONE_PROMOTION_BOON;
        }
        return handleInsufficientPromotionProduct(productName, currentQuantity);
    }

    private int handleInsufficientPromotionProduct(String productName, int currentQuantity) {
        Optional<Product> productOpt = inventoryManager.findProductWithoutPromotion(productName);
        Product productWithoutPromotion = productOpt.get();
        int requiredQuantity = Math.abs(currentQuantity);
        inventoryManager.reduceProductQuantity(productWithoutPromotion, requiredQuantity);
        return NO_ANY_PROMOTION_BOON;
    }

    private int processPurchaseQuantity(String productName, int purchaseQuantity,
                                        Product productWithPromotion, int promotionBoon) {
        if (isSpecialCase(purchaseQuantity, promotionBoon)) {
            return handleSpecialCase(productName, purchaseQuantity, productWithPromotion, promotionBoon);
        }
        return handleGreaterThanPromotionBoonCase(productName, purchaseQuantity, productWithPromotion, promotionBoon);
    }

    private boolean isSpecialCase(int purchaseQuantity, int promotionBoon) {
        return purchaseQuantity == promotionBoon ||
            purchaseQuantity < promotionBoon - GET_ONE_FREE ||
            purchaseQuantity == promotionBoon - GET_ONE_FREE;
    }

    private int handleSpecialCase(String productName, int purchaseQuantity,
                                Product productWithPromotion, int promotionBoon) {
        if (purchaseQuantity == promotionBoon)
            return handleEqualPromotionBoonCase(purchaseQuantity, productWithPromotion);
        if (purchaseQuantity < promotionBoon - GET_ONE_FREE)
            return handleLessThanPromotionBoonMinusOneCase(purchaseQuantity, productWithPromotion);
        return handleEqualPromotionBoonMinusOneCase(productName, purchaseQuantity, productWithPromotion);
    }

    private int handleEqualPromotionBoonCase(int purchaseQuantity, Product productWithPromotion) {
        productWithPromotion.reduceQuantity(purchaseQuantity);
        return ONE_PROMOTION_BOON;
    }

    private int handleLessThanPromotionBoonMinusOneCase(int purchaseQuantity, Product productWithPromotion) {
        productWithPromotion.reduceQuantity(purchaseQuantity);
        return NO_ANY_PROMOTION_BOON;
    }

    private int handleEqualPromotionBoonMinusOneCase(String productName, int purchaseQuantity,
                                                     Product productWithPromotion) {
        String answer = userInteractionHandler.getValidatedAnswerForOneMoreProduct(productName);
        if (answer.equals(AnswerConstants.ANSWER_YES.getConstants())) {
            productWithPromotion.reduceQuantity(purchaseQuantity + ONE_PROMOTION_BOON);
            return ONE_PROMOTION_BOON;
        }
        productWithPromotion.reduceQuantity(purchaseQuantity);
        return NO_ANY_PROMOTION_BOON;
    }

    private int handleGreaterThanPromotionBoonCase(String productName, int purchaseQuantity,
                                               Product productWithPromotion, int promotionBoon) {
        int result = NO_ANY_PROMOTION_BOON;
        int currentPurchaseQuantity = purchaseQuantity;
        result += calculatePromotionBoons(productName, productWithPromotion, promotionBoon, currentPurchaseQuantity);
    
        return result;
    }

    private int calculatePromotionBoons(String productName, Product productWithPromotion, 
                                        int promotionBoon, int currentPurchaseQuantity) {
        int result = NO_ANY_PRODUCT;
        while (currentPurchaseQuantity > NO_ANY_PRODUCT) {
            currentPurchaseQuantity = processSinglePromotionCycle(productName, productWithPromotion, 
                                                                promotionBoon, currentPurchaseQuantity, result);
            result += ONE_PROMOTION_BOON;
        }
        return result;
    }

    private int processSinglePromotionCycle(String productName, Product productWithPromotion, 
                                            int promotionBoon, int currentPurchaseQuantity, int result) {
        int currentQuantity = productWithPromotion.firstReduceQuantityThanCheck(promotionBoon);
        currentPurchaseQuantity -= promotionBoon; 
        if (currentQuantity < NO_ANY_PRODUCT) {
            return handleInsufficientQuantityInLoop(productName, currentQuantity, 
                                                    currentPurchaseQuantity, result);
        }   
        return currentPurchaseQuantity;
    }

    private int handleInsufficientQuantityInLoop(String productName, int currentQuantity,
                                                 int currentPurchaseQuantity, int result) {
        currentPurchaseQuantity += GET_ONE_FREE;
        int lackQuantity = handleLackOfQuantity(productName, currentQuantity, currentPurchaseQuantity);
        return result + lackQuantity;
    }

    private int handleLackOfQuantity(String productName, int currentQuantity, int currentPurchaseQuantity) {
        String answer = userInteractionHandler.getValidatedAnswerToLackOfQuantity(productName, currentPurchaseQuantity);
        return processLackOfQuantityAnswer(productName, currentQuantity, answer);
    }

    private int processLackOfQuantityAnswer(String productName, int currentQuantity, String answer) {
        if (answer.equals(AnswerConstants.ANSWER_YES.getConstants())) {
            handleValidQuantityReduction(productName, currentQuantity);
            return NO_ANY_PRODUCT;
        }
        if (answer.equals(AnswerConstants.ANSWER_NO.getConstants())) {
            return currentQuantity;
        }
        return NO_ANY_PRODUCT;
    }

    private void handleValidQuantityReduction(String productName, int currentQuantity) {
        Optional<Product> productOpt = inventoryManager.findProductWithoutPromotion(productName);
        Product productWithoutPromotion = productOpt.get();
        int requiredQuantity = Math.abs(currentQuantity) + GET_ONE_FREE;
        inventoryManager.reduceProductQuantity(productWithoutPromotion, requiredQuantity);
    }
}