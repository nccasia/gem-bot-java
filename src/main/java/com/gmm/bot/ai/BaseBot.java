package com.gmm.bot.ai;

import com.gmm.bot.enumeration.BattleMode;
import com.gmm.bot.enumeration.GemType;
import com.gmm.bot.model.Grid;
import com.gmm.bot.model.Hero;
import com.gmm.bot.model.Pair;
import com.gmm.bot.model.Player;
import com.smartfoxserver.v2.entities.data.ISFSArray;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import sfs2x.client.SmartFox;
import sfs2x.client.core.BaseEvent;
import sfs2x.client.core.IEventListener;
import sfs2x.client.core.SFSEvent;
import sfs2x.client.entities.Room;
import sfs2x.client.entities.User;
import sfs2x.client.requests.ExtensionRequest;
import sfs2x.client.requests.JoinRoomRequest;
import sfs2x.client.requests.LoginRequest;
import sfs2x.client.util.ConfigData;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Getter
public abstract class BaseBot implements IEventListener {
    private final int ENEMY_PLAYER_ID = 0;
    private final int BOT_PLAYER_ID = 2;
    @Autowired
    protected ThreadPoolTaskScheduler taskScheduler;
    @Value("${smartfox.host}")
    protected String host;
    @Value("${smartfox.zone}")
    protected String zone;
    @Value("${smartfox.port}")
    protected int port;
    @Value("${gemswap.delay}")
    protected int delaySwapGem;
    @Value("${find.game.delay}")
    protected int delayFindGame;
    protected SmartFox sfsClient;
    protected Room room;
    protected Player botPlayer;
    protected Player enemyPlayer;
    protected int currentPlayerId;
    protected Grid grid;
    protected volatile boolean isJoinGameRoom;
    protected String username;
    protected String token;
    protected SFSObject data;
    protected boolean disconnect;

    public void start() {
        try {
            this.logStatus("init", "Initializing");
            this.init();
            this.connect();
        } catch (Exception e) {
            this.log("Init bot error =>" + e.getMessage());
        }
    }

    private void init() {
        username = "bot_" + UUID.randomUUID();
        sfsClient = new SmartFox();
        data = new SFSObject();
        isJoinGameRoom = false;
        disconnect = false;

        this.sfsClient.addEventListener(SFSEvent.CONNECTION, this);
        this.sfsClient.addEventListener(SFSEvent.CONNECTION_LOST, this);
        this.sfsClient.addEventListener(SFSEvent.LOGIN, this);
        this.sfsClient.addEventListener(SFSEvent.LOGIN_ERROR, this);
        this.sfsClient.addEventListener(SFSEvent.ROOM_JOIN, this);
        this.sfsClient.addEventListener(SFSEvent.ROOM_JOIN_ERROR, this);
        this.sfsClient.addEventListener(SFSEvent.EXTENSION_RESPONSE, this);
    }

    protected void connect() {
        this.logStatus("connecting", " => Connecting to smartfox server " + host + "|" + port + " zone: " + zone);

        this.sfsClient.setUseBlueBox(true);
        this.sfsClient.connect(this.host, this.port);

        ConfigData cf = new ConfigData();
        cf.setHost(host);
        cf.setPort(port);
        cf.setUseBBox(true);
        cf.setZone(zone);

        try {
            this.sfsClient.connect(cf);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        this.logStatus("disconnect|", " manual called disconnect from client");
        try {
            sfsClient.disconnect();
            disconnect = true;
        } catch (Exception e) {
            log.error("disconnect|" + this.username + "|error =>" + e.getMessage());
        }
    }

    public void dispatch(BaseEvent event) {
        String eventType = event.getType();

        switch (eventType) {
            case SFSEvent.CONNECTION:
                this.onConnection(event);
                break;
            case SFSEvent.CONNECTION_LOST:
                this.onConnectionLost(event);
                break;
            case SFSEvent.LOGIN:
                this.onLoginSuccess(event);
                break;
            case SFSEvent.LOGIN_ERROR:
                this.onLoginError(event);
                break;
            case SFSEvent.ROOM_JOIN:
                this.onRoomJoin(event);
                break;
            case SFSEvent.ROOM_JOIN_ERROR:
                this.onRoomJoinError(event);
                break;
            case SFSEvent.EXTENSION_RESPONSE:
                this.onExtensionResponse(event);
                break;
            default:
        }
    }

    private void onConnection(BaseEvent event) {
        if (event.getArguments().get("success").equals(true)) {
            this.logStatus("try-login", "Connected to smartfox|" + event.getArguments().toString());
            this.login();
        } else {
            this.logStatus("onConnection|success == false", "Failed to connect");
        }
    }

    protected void onConnectionLost(BaseEvent event) {
        this.logStatus("onConnectionLost", "userId connection lost server: " + event.getArguments().toString());
        disconnect = true;
        sfsClient.removeAllEventListeners();
    }


    protected void onLoginError(BaseEvent event) {
        this.logStatus("login-error", "Login failed");
        disconnect();
    }

    protected void onRoomJoin(BaseEvent event) {
        logStatus("Join-room", "Joined room " + this.sfsClient.getLastJoinedRoom().getName());
        room = (Room) event.getArguments().get("room");
        if (!room.getName().equals("lobby")) {
            return;
        }
        taskScheduler.schedule(new FindRoomGame(), new Date(System.currentTimeMillis() + delayFindGame));

    }

    protected void onRoomJoinError(BaseEvent event) {
        if (this.sfsClient.getLastJoinedRoom() != null) {
            this.logStatus("join-room", "Joined room " + this.sfsClient.getLastJoinedRoom().getName());
        }
        taskScheduler.schedule(new FindRoomGame(), new Date(System.currentTimeMillis() + delayFindGame));
    }

    protected void onExtensionResponse(BaseEvent event) {
        String cmd = event.getArguments().containsKey("cmd") ? event.getArguments().get("cmd").toString() : "";
        SFSObject params = (SFSObject) event.getArguments().get("params");
        switch (cmd) {
            case ConstantCommand.START_GAME:
                ISFSObject gameSession = params.getSFSObject("gameSession");
                startGame(gameSession, room);
                break;
            case ConstantCommand.END_GAME:
                endGame();
                break;
            case ConstantCommand.START_TURN:
                startTurn(params);
                break;
            case ConstantCommand.ON_SWAP_GEM:
                swapGem(params);
                break;
            case ConstantCommand.ON_PLAYER_USE_SKILL:
                handleGems(params);
                break;
            case ConstantCommand.PLAYER_JOINED_GAME:
                sendExtensionRequest(ConstantCommand.I_AM_READY, new SFSObject());
                break;
        }

    }

    protected void swapGem(SFSObject params) {
        boolean isValidSwap = params.getBool("validSwap");
        if (!isValidSwap) {
            return;
        }
        handleGems(params);
    }

    private void handleHeroes(ISFSObject params) {
        ISFSArray heroesBotPlayer = params.getSFSArray(botPlayer.getDisplayName());
        for (int i = 0; i < botPlayer.getHeroes().size(); i++) {
            botPlayer.getHeroes().get(i).updateHero(heroesBotPlayer.getSFSObject(i));
        }

        ISFSArray heroesEnemyPlayer = params.getSFSArray(enemyPlayer.getDisplayName());
        for (int i = 0; i < enemyPlayer.getHeroes().size(); i++) {
            enemyPlayer.getHeroes().get(i).updateHero(heroesEnemyPlayer.getSFSObject(i));
        }
    }

    protected void handleGems(ISFSObject params) {
        ISFSObject gameSession = params.getSFSObject("gameSession");
        currentPlayerId = gameSession.getInt("currentPlayerId");
        //get last snapshot
        ISFSArray snapshotSfsArray = params.getSFSArray("snapshots");
        ISFSObject lastSnapshot = snapshotSfsArray.getSFSObject(snapshotSfsArray.size() - 1);
        boolean needRenewBoard = params.containsKey("renewBoard");
        // update information of hero
        handleHeroes(lastSnapshot);
        if (needRenewBoard) {
            grid.updateGems(params.getSFSArray("renewBoard"));
            taskScheduler.schedule(new FinishTurn(false), new Date(System.currentTimeMillis() + delaySwapGem));
            return;
        }
        // update gem
        grid.setGemTypes(botPlayer.getRecommendGemType());
        grid.updateGems(lastSnapshot.getSFSArray("gems"));
        taskScheduler.schedule(new FinishTurn(false), new Date(System.currentTimeMillis() + delaySwapGem));
    }

    protected void startTurn(ISFSObject params) {
        currentPlayerId = params.getInt("currentPlayerId");
        if (!isBotTurn()) {
            return;
        }
        Optional<Hero> heroFullMana = botPlayer.anyHeroFullMana();
        if (heroFullMana.isPresent()) {
            taskScheduler.schedule(new SendReQuestSkill(heroFullMana.get()), new Date(System.currentTimeMillis() + delaySwapGem));
            return;
        }
        taskScheduler.schedule(new SendRequestSwapGem(), new Date(System.currentTimeMillis() + delaySwapGem));
    }

    protected GemType selectGem() {
        return botPlayer.getRecommendGemType().stream().filter(gemType -> grid.getGemTypes().contains(gemType)).findFirst().orElseGet(null);
    }

    protected boolean isBotTurn() {
        return botPlayer.getId() == currentPlayerId;
    }

    protected void endGame() {
        isJoinGameRoom = false;
    }

    protected void startGame(ISFSObject gameSession, Room room) {
        // Assign Bot player & enemy player
        assignPlayers(room);

        // Player & Heroes
        ISFSObject objBotPlayer = gameSession.getSFSObject(botPlayer.getDisplayName());
        ISFSObject objEnemyPlayer = gameSession.getSFSObject(enemyPlayer.getDisplayName());

        ISFSArray botPlayerHero = objBotPlayer.getSFSArray("heroes");
        ISFSArray enemyPlayerHero = objEnemyPlayer.getSFSArray("heroes");

        for (int i = 0; i < botPlayerHero.size(); i++) {
            botPlayer.getHeroes().add(new Hero(botPlayerHero.getSFSObject(i)));
        }
        for (int i = 0; i < enemyPlayerHero.size(); i++) {
            enemyPlayer.getHeroes().add(new Hero(enemyPlayerHero.getSFSObject(i)));
        }

        // Gems
        grid = new Grid(gameSession.getSFSArray("gems"), botPlayer.getRecommendGemType());
        currentPlayerId = gameSession.getInt("currentPlayerId");
        log("Initial game ");
        taskScheduler.schedule(new FinishTurn(true), new Date(System.currentTimeMillis() + delaySwapGem));
    }

    protected void assignPlayers(Room room) {
        User user1 = room.getPlayerList().get(0);
        log("id user1: " + user1.getPlayerId());
        if (user1.isItMe()) {
            botPlayer = new Player(user1.getPlayerId(), "player1");
            enemyPlayer = new Player(ENEMY_PLAYER_ID, "player2");
        } else {
            botPlayer = new Player(BOT_PLAYER_ID, "player2");
            enemyPlayer = new Player(ENEMY_PLAYER_ID, "player1");
        }
    }

    protected void logStatus(String status, String logMsg) {
        log.info(this.username + "|" + status + "|" + logMsg + "\n");
    }

    protected void log(String msg) {
        log.info(this.username + "|" + msg);
    }

    private void onLoginSuccess(BaseEvent event) {
        try {
            log("onLogin()|" + event.getArguments().toString());

            // Find game after login
            data.putUtfString("type", "");
            data.putUtfString("adventureId", "");
            sendZoneExtensionRequest(ConstantCommand.LOBBY_FIND_GAME, data);

        } catch (Exception e) {
            log("onLogin|error => " + e.getMessage());
            e.printStackTrace();
        }
    }

    protected void login() {
        log("login()");
        this.token = "bot";
        SFSObject parameters = new SFSObject();
        parameters.putUtfString(ConstantCommand.BATTLE_MODE, BattleMode.NORMAL.name());
        parameters.putUtfString(ConstantCommand.ID_TOKEN, this.token);
        this.sfsClient.send(new LoginRequest(username, "", zone, parameters));
    }

    public void sendExtensionRequest(String extCmd, ISFSObject params) {
        this.sfsClient.send(new ExtensionRequest(extCmd, params, room));
    }

    public void sendZoneExtensionRequest(String extCmd, ISFSObject params) {
        this.sfsClient.send(new ExtensionRequest(extCmd, params));
    }

    private class FindRoomGame implements Runnable {
        @Override
        public void run() {
            List<Room> rooms = sfsClient.getRoomList();
            List<Room> joinedRooms = sfsClient.getJoinedRooms();
            Optional<Room> botRoom = rooms.stream().filter(room1 -> room1.isGame() && room1.getUserCount() == 1 && !joinedRooms.contains(room1)).findFirst(); //&& room1.getName().startsWith("bot") && room1.isHidden()
            if (botRoom.isPresent()) {
                sfsClient.send(new JoinRoomRequest(botRoom.get()));
                isJoinGameRoom = true;
            }
        }
    }

    private class FinishTurn implements Runnable {
        private final boolean isFirstTurn;

        public FinishTurn(boolean isFirstTurn) {
            this.isFirstTurn = isFirstTurn;
        }

        @Override
        public void run() {
            SFSObject data = new SFSObject();
            data.putBool("isFirstTurn", isFirstTurn);
            log("sendExtensionRequest()|room:" + room.getName() + "|extCmd:" + ConstantCommand.FINISH_TURN + " first turn " + isFirstTurn);
            sendExtensionRequest(ConstantCommand.FINISH_TURN, data);
        }
    }

    private class SendReQuestSkill implements Runnable {
        private final Hero heroCastSkill;

        public SendReQuestSkill(Hero heroCastSkill) {
            this.heroCastSkill = heroCastSkill;
        }

        @Override
        public void run() {
            data.putUtfString("casterId", heroCastSkill.getId().toString());
            if (heroCastSkill.isHeroSelfSkill()) {
                data.putUtfString("targetId", botPlayer.firstHeroAlive().getId().toString());
            } else {
                data.putUtfString("targetId", enemyPlayer.firstHeroAlive().getId().toString());
            }
            data.putUtfString("selectedGem", String.valueOf(selectGem().getCode()));
            data.putUtfString("gemIndex", String.valueOf(ThreadLocalRandom.current().nextInt(64)));
            log("sendExtensionRequest()|room:" + room.getName() + "|extCmd:" + ConstantCommand.USE_SKILL + "|Hero cast skill: " + heroCastSkill.getName());
            sendExtensionRequest(ConstantCommand.USE_SKILL, data);
        }

    }

    private class SendRequestSwapGem implements Runnable {
        @Override
        public void run() {
            Pair<Integer> indexSwap = grid.recommendSwapGem();
            data.putInt("index1", indexSwap.getParam1());
            data.putInt("index2", indexSwap.getParam2());
            log("sendExtensionRequest()|room:" + room.getName() + "|extCmd:" + ConstantCommand.SWAP_GEM + "|index1: " + indexSwap.getParam1() + " index1: " + indexSwap.getParam2());
            sendExtensionRequest(ConstantCommand.SWAP_GEM, data);
        }
    }
}