package com.goldenrace.wallet.server;

import com.fasterxml.jackson.databind.JsonNode;
import com.goldenrace.wallet.server.api.WalletApi;
import com.goldenrace.wallet.server.api.model.ModelJson;
import com.goldenrace.wallet.server.properties.logging.AppLog;
import com.goldenrace.wallet.server.properties.logging.AppLogger;
import com.goldenrace.wallet.server.properties.logging.IAppLogger;
import com.goldenrace.wallet.server.properties.Settings;
import org.joda.time.DateTime;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class WalletApiServerController implements WalletApi {

    private static final IAppLogger LOGGER = AppLogger.getLogger(WalletApiServerController.class);

    private static final double DEFAULT_CREDIT   = 10; //100_000D
    private static final String DEFAULT_CURRENCY = "EUR"; //BTC

    private static final boolean SOLVE_CREDIT = true;

    // Nodes
    protected static final String AMOUNT             = "amount";
    protected static final String CODE               = "code";
    protected static final String CREDIT             = "credit";
    protected static final String CREDIT_AMOUNT      = "creditAmount";
    protected static final String CURRENCY           = "currency";
    protected static final String CURRENCY_CODE      = "currencyCode";
    protected static final String ENTITY_ID          = "entityId";
    protected static final String ERROR_ID           = "errorId";
    protected static final String ERROR_MESSAGE      = "errorMessage";
    protected static final String EXT_ID             = "extId";
    protected static final String EXT_TICKET_ID      = "extTicketId";
    protected static final String EXT_TOKEN          = "extToken";
    protected static final String EXT_TRANSACTION_ID = "extTransactionID";
    protected static final String EXT_DATA           = "extData";
    protected static final String EXT_WALLET_ID      = "extWalletId";
    protected static final String LAST_SEEN          = "lastSeen";
    protected static final String NEW_CREDIT         = "newCredit";
    protected static final String OLD_CREDIT         = "oldCredit";
    protected static final String RESULT             = "result";
    protected static final String STAKE              = "stake";
    protected static final String STAKE_TAXES        = "stakeTaxes";
    protected static final String STATUS             = "status";
    protected static final String TICKET             = "ticket";
    protected static final String TICKET_ID          = "ticketId";
    protected static final String TOTAL_CREDITED     = "totalCredited";
    protected static final String TYPE               = "type";
    protected static final String UNIT_ID            = "unitId";
    protected static final String WON_AMOUNT         = "wonAmount";
    protected static final String WON_BONUS          = "wonBonus";
    protected static final String WON_DATA           = "wonData";
    protected static final String WON_JACKPOT        = "wonJackpot";

    // Types
    protected static final String CREDIT_RESPONSE        = "CreditResponse";
    protected static final String REGISTER_RESPONSE      = "RegisterResponse";
    protected static final String SELL_RESPONSE          = "SellResponse";
    protected static final String WALLET_CREDIT_RESPONSE = "WalletCreditResponse";
    protected static final String WALLET_LOGIN_RESPONSE  = "WalletLoginResponse";
    protected static final String WALLET_RESPONSE        = "WalletResponse";

    // Results
    protected static final String RESULT_SUCCESS = "success";

    // Ticket status
    protected static final String STATUS_PAIDOUT = "PAIDOUT";
    protected static final String STATUS_WON     = "WON";

    // Credit
    private final Map<String, Double> creditMap = new ConcurrentHashMap<>();

    // if it is true, the sell method will respond with an error, Gol-153377
    private final boolean sellWrongResponse;

    public WalletApiServerController(Settings settings) {
        sellWrongResponse = settings.getBoolean("sell.wrong.response", false);
    }

    @Override
    public ResponseEntity<List<JsonNode>> sessionKeepAlive(@RequestBody List<JsonNode> bulkRequestKeep) {
        List<JsonNode> responses = new ArrayList<>();
        for (JsonNode req : bulkRequestKeep) {
            try {
                ModelJson reqJson = new ModelJson(req);
                if (reqJson.getDateTime(LAST_SEEN).plusMinutes(5).getMillis() < DateTime.now().getMillis()) {
                    ModelJson resJson = new ModelJson();
                    resJson.putString(EXT_TOKEN, reqJson.getString(EXT_TOKEN));
                    responses.add(resJson.getJsonNode());
                }
            } catch (IOException ex) {
                LOGGER.error(AppLog.Builder.id("sessionKeepAlive").message(ex.getMessage()), ex);
            }
        }
        return ResponseEntity.ok(responses);
    }

    @Override
    public ResponseEntity<JsonNode> sessionLogin(@RequestBody JsonNode loginRequest) {
        Double userCredit = 10D;
        // try {
        //     ModelJson reqJson = new ModelJson(loginRequest);
        //     userCredit = getCredit(reqJson.getInteger(UNIT_ID));
        // } catch (IOException ex) {
        //     LOGGER.error(AppLog.Builder.id("sessionLogin").message(ex.getMessage()), ex);
        // }
        ModelJson resJson = new ModelJson();
        resJson.putString(TYPE, WALLET_LOGIN_RESPONSE);
        resJson.putString(EXT_TOKEN, UUID.randomUUID().toString());
        resJson.putString(CURRENCY_CODE, DEFAULT_CURRENCY);
        resJson.putDouble(CREDIT, userCredit);
        //Testing extWalletId
        //resJson.putString("extWalletId", "ext_2");
        return ResponseEntity.ok(resJson.getJsonNode());
    }

    @Override
    public ResponseEntity<Void> sessionLogout(@RequestBody List<JsonNode> bulkRequestLogout) {
        // do some magic!
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<List<JsonNode>> walletCancel(@RequestBody List<JsonNode> bulkRequestCancel) {
        List<JsonNode> responses = new ArrayList<>();
        for (JsonNode cancel : bulkRequestCancel) {
            try {
                ModelJson reqJson      = new ModelJson(cancel);
                Integer   entityId     = reqJson.getInteger(ENTITY_ID);
                Double    actualCredit = 0.0;
                Double    newCredit    = 0.0;
                updateCredit(entityId, newCredit);

                ModelJson resJson  = new ModelJson();
                Long      ticketId = reqJson.getLong(TICKET_ID);
                resJson.putString(TYPE, WALLET_CREDIT_RESPONSE);
                resJson.putLong(TICKET_ID, ticketId);
                resJson.putDouble(NEW_CREDIT, newCredit);
                resJson.putDouble(OLD_CREDIT, actualCredit);
                resJson.putString(RESULT, RESULT_SUCCESS);
                resJson.putString(EXT_TRANSACTION_ID, "CANCEL_" + ticketId);
                responses.add(resJson.getJsonNode());
            } catch (IOException ex) {
                LOGGER.error(AppLog.Builder.id("walletCancel").message(ex.getMessage()), ex);
            }
        }
        return ResponseEntity.ok(responses);
    }

    @Override
    public ResponseEntity<List<JsonNode>> walletCredit(@RequestBody List<JsonNode> bulkRequestCredit) {
        List<JsonNode> responses = new ArrayList<>();
        for (JsonNode creditRequest : bulkRequestCredit) {
            try {
                ModelJson reqJson      = new ModelJson(creditRequest);
                Double    actualCredit = 0.0;
                ModelJson resJson      = new ModelJson();
                resJson.putString(TYPE, CREDIT_RESPONSE);
                resJson.putDouble(CREDIT, actualCredit);
                resJson.putString(CURRENCY_CODE, reqJson.getString(CURRENCY_CODE));
                resJson.putString(EXT_ID, reqJson.getString(EXT_ID));
                responses.add(resJson.getJsonNode());
            } catch (IOException ex) {
                LOGGER.error(AppLog.Builder.id("walletCredit").message(ex.getMessage()), ex);
            }
        }
        return ResponseEntity.ok(responses);
    }

    @Override
    public ResponseEntity<List<JsonNode>> walletPayout(@RequestBody List<JsonNode> bulkRequestPayout) {
        List<JsonNode> responses = new ArrayList<>();
        for (JsonNode payoutRequest : bulkRequestPayout) {
            try {
                ModelJson reqJson     = new ModelJson(payoutRequest);
                JsonNode  wonDataNode = reqJson.getJsonNode(TICKET, WON_DATA);
                Double    pending     = getPending(reqJson, wonDataNode);
                Long      ticketId    = reqJson.getLong(TICKET_ID);
                ModelJson resJson;
                if (Objects.nonNull(pending) && pending > 0) {
                    Integer entityId     = reqJson.getInteger(ENTITY_ID);
                    Double  actualCredit = getCredit(entityId);
                    Double  newCredit    = actualCredit + reqJson.getDouble(AMOUNT);
                    updateCredit(entityId, newCredit);
                    resJson = generateWalletCreditResponse(actualCredit, newCredit, ticketId, pending);
                } else {
                    resJson = generateWalletCreditResponse(null, null, ticketId, pending);
                }
                responses.add(resJson.getJsonNode());
            } catch (IOException ex) {
                LOGGER.error(AppLog.Builder.id("walletPayout").message(ex.getMessage()), ex);
            }
        }
        return ResponseEntity.ok(responses);
    }

    @Override
    public ResponseEntity<List<JsonNode>> walletRegister(@RequestBody List<JsonNode> bulkRequestRegister) {
        List<JsonNode> responses = new ArrayList<>();
        for (JsonNode registerRequest : bulkRequestRegister) {
            try {
                ModelJson reqJson    = new ModelJson(registerRequest);
                ModelJson ticketJson = new ModelJson(reqJson.getJsonNode(TICKET));

                ModelJson resJson  = new ModelJson();
                Long      ticketId = reqJson.getLong(TICKET_ID);
                resJson.putString(TYPE, REGISTER_RESPONSE);
                resJson.putLong(TICKET_ID, ticketId);
                resJson.putString(EXT_TICKET_ID, "EXT_" + ticketId);
                resJson.putString(RESULT, RESULT_SUCCESS);
                resJson.putLong(ERROR_ID, 0L);
                resJson.putString(ERROR_MESSAGE, "");

                Double amount           = reqJson.getDouble(AMOUNT, 0D);
                String extTransactionId = "REGISTER_ZERO";
                if (amount > 0) {
                    extTransactionId = "REGISTER_" + ticketId;
                } else if (amount < 0) {
                    extTransactionId = "REGISTER_PAYOUT_" + ticketId;
                }
                resJson.putString(EXT_TRANSACTION_ID, extTransactionId);

                String  currency = getCurrency(ticketJson);
                Integer entityId = reqJson.getInteger(ENTITY_ID);

                Double actualCredit = getCredit(entityId, currency);
                Double newCredit    = BigDecimal.valueOf(actualCredit).add(BigDecimal.valueOf(amount)).doubleValue();

                updateCredit(entityId, currency, newCredit);

                resJson.putDouble(OLD_CREDIT, actualCredit);
                resJson.putDouble(NEW_CREDIT, newCredit);

                responses.add(resJson.getJsonNode());
            } catch (IOException ex) {
                LOGGER.error(AppLog.Builder.id("walletRegister").message(ex.getMessage()), ex);
            }
        }
        return ResponseEntity.ok(responses);
    }

    @Override
    public ResponseEntity<List<JsonNode>> walletSell(@RequestBody List<JsonNode> bulkRequestSell) {
        List<JsonNode> responses = new ArrayList<>();
        for (JsonNode sellRequest : bulkRequestSell) {
            try {
                ModelJson reqJson      = new ModelJson(sellRequest);
                Integer   entityId     = reqJson.getInteger(ENTITY_ID);
                Double    actualCredit = getCredit(entityId);
                ModelJson ticketJson   = new ModelJson(reqJson.getJsonNode(TICKET));
                Double    newCredit    = actualCredit - ticketJson.getDouble(STAKE, 0D) - ticketJson.getDouble(STAKE_TAXES, 0D);
                updateCredit(entityId, newCredit);

                ModelJson resJson  = new ModelJson();
                Long      ticketId = reqJson.getLong(TICKET_ID);
                resJson.putString(TYPE, SELL_RESPONSE);
                resJson.putLong(TICKET_ID, ticketId);
                resJson.putString(EXT_TICKET_ID, "EXT_" + ticketId);
                resJson.putString(RESULT, RESULT_SUCCESS);
                resJson.putString(EXT_WALLET_ID, "1234566");
                resJson.putDouble(NEW_CREDIT, newCredit);
                resJson.putDouble(OLD_CREDIT, actualCredit);
                resJson.putString(EXT_TRANSACTION_ID, "SELL_" + ticketId);
                resJson.putString(EXT_DATA, "Test");
                //resJson.putString(ERROR_MESSAGE, "SUCCESS");

                if (sellWrongResponse) {
                    resJson.putString(RESULT, "error");
                    resJson.putInteger(ERROR_ID, 100);
                    resJson.putString(ERROR_MESSAGE, "Unexpected error");
                    resJson.putString("extTranctionData", "RESPONSIBLE_GAMING_LOSS_LIMIT");
                }

                responses.add(resJson.getJsonNode());
            } catch (IOException ex) {
                LOGGER.error(AppLog.Builder.id("walletCredit").message(ex.getMessage()), ex);
            }
        }
        return ResponseEntity.ok(responses);
    }

    @Override
    public ResponseEntity<List<JsonNode>> walletSolve(@RequestBody List<JsonNode> bulkRequestSolve) {
        List<JsonNode> responses = new ArrayList<>();
        for (JsonNode solveRequest : bulkRequestSolve) {
            try {
                ModelJson reqJson    = new ModelJson(solveRequest);
                ModelJson ticketJson = new ModelJson(reqJson.getJsonNode(TICKET));
                String    status     = ticketJson.getString(STATUS);
                Long      ticketId   = reqJson.getLong(TICKET_ID);
                ModelJson resJson;
                if (SOLVE_CREDIT && Objects.nonNull(status) && (STATUS_WON.equals(status) || STATUS_PAIDOUT.equals(status))) {
                    Integer entityId     = reqJson.getInteger(ENTITY_ID);
                    Double  actualCredit = getCredit(entityId);
                    Double  newCredit    = actualCredit + getTicketWonAmount(reqJson);
                    updateCredit(entityId, newCredit);
                    resJson = generateWalletCreditResponse(actualCredit, newCredit, ticketId, null);
                } else {
                    resJson = generateWalletResponse(ticketId);
                }
                responses.add(resJson.getJsonNode());
            } catch (IOException ex) {
                LOGGER.error(AppLog.Builder.id("walletSolve").message(ex.getMessage()), ex);
            }
        }
        return ResponseEntity.ok(responses);
    }

    private Double getCredit(Integer entityId, String currency) {
        String key = getKeyCredit(entityId, currency);
        if (creditMap.containsKey(key)) {
            return creditMap.get(key);
        } else {
            creditMap.put(key, DEFAULT_CREDIT);
            return DEFAULT_CREDIT;
        }
    }

    private Double getCredit(Integer entityId) {
        return getCredit(entityId, DEFAULT_CURRENCY);
    }

    private void updateCredit(Integer entityId, String currency, Double userCredit) {
        String key = getKeyCredit(entityId, currency);
        if (creditMap.containsKey(key)) {
            creditMap.put(key, userCredit);
        } else {
            creditMap.put(key, DEFAULT_CREDIT + userCredit);
        }
    }

    private void updateCredit(Integer entityId, Double userCredit) {
        updateCredit(entityId, DEFAULT_CURRENCY, userCredit);
    }

    private String getKeyCredit(Integer entityId, String currency) {
        return entityId + "_" + currency;
    }

    private ModelJson generateWalletCreditResponse(Double actualCredit, Double newCredit, Long ticketId, Double creditAmount) {
        ModelJson resJson = new ModelJson();
        resJson.putString(TYPE, WALLET_CREDIT_RESPONSE);
        resJson.putDouble(OLD_CREDIT, actualCredit);
        resJson.putString(EXT_WALLET_ID, "1234566");
        resJson.putDouble(NEW_CREDIT, newCredit);
        resJson.putLong(TICKET_ID, ticketId);
        resJson.putString(RESULT, RESULT_SUCCESS);
        resJson.putString(EXT_TRANSACTION_ID, "PAY_" + ticketId);
        resJson.putString(ERROR_MESSAGE, "SUCCESS");
        //if (Objects.isNull(creditAmount)) {
            //resJson.putDouble(CREDIT_AMOUNT, newCredit - actualCredit);
        //} else {
            //resJson.putDouble(CREDIT_AMOUNT, creditAmount);
        //}
        return resJson;
    }

    private ModelJson generateWalletResponse(Long ticketId) {
        ModelJson resJson = new ModelJson();
        resJson.putString(TYPE, WALLET_RESPONSE);
        resJson.putString(RESULT, RESULT_SUCCESS);
        resJson.putLong(TICKET_ID, ticketId);
        return resJson;
    }

    private String getCurrency(ModelJson ticketJson) {
        try {
            JsonNode  currencyJsonNode = ticketJson.getJsonNode(CURRENCY);
            ModelJson currencyJson     = new ModelJson(currencyJsonNode);
            return currencyJson.getString(CODE, DEFAULT_CURRENCY);
        } catch (IOException ex) {
            return DEFAULT_CURRENCY;
        }
    }

    private Double getPending(ModelJson reqJson, JsonNode wonDataNode) {
        Double  pending        = null;
        boolean needSetPending = false;
        try {
            ModelJson wonDataJson  = new ModelJson(wonDataNode);
            Double    creditSolved = wonDataJson.getDouble(TOTAL_CREDITED);
            if (Objects.nonNull(creditSolved)) {
                Double bonus   = wonDataJson.getDouble(WON_BONUS, 0D);
                Double won     = wonDataJson.getDouble(WON_AMOUNT, 0D);
                Double jackpot = wonDataJson.getDouble(WON_JACKPOT, 0D);
                pending = won + bonus + jackpot - creditSolved;
            } else {
                needSetPending = true;
            }
        } catch (IOException ex) {
            needSetPending = true;
        }
        if (needSetPending) {
            pending = reqJson.getDouble(AMOUNT);
        }
        return pending;
    }

    private Double getTicketWonAmount(ModelJson reqJson) {
        JsonNode wonDataNode = reqJson.getJsonNode(TICKET, WON_DATA);
        try {
            // get the credited amount, that amount was notified in a previous wallet operation
            ModelJson wonDataJson    = new ModelJson(wonDataNode);
            Double    creditedAmount = wonDataJson.getDouble(TOTAL_CREDITED);
            Double    bonusAmount    = wonDataJson.getDouble(WON_BONUS, 0D);
            Double    wonAmount      = wonDataJson.getDouble(WON_AMOUNT, 0D);
            return wonAmount + bonusAmount - creditedAmount;
        } catch (IOException ex) {
            return 0D;
        }
    }

}
