    package com.example.VotingApp.controllers;

    import com.example.VotingApp.models.Block;
    import com.example.VotingApp.models.ConsensusMessage;
    import jakarta.annotation.PostConstruct;
    import org.springframework.beans.factory.annotation.Value;
    import org.springframework.http.ResponseEntity;
    import org.springframework.web.bind.annotation.*;
    import org.springframework.web.client.RestTemplate;

    import java.util.ArrayList;
    import java.util.Iterator;
    import java.util.List;
    import java.util.Map;

    @CrossOrigin(origins = "http://localhost:3000")
    @RestController
    public class PBFTController {

        // Blockchain-ul în memorie
        private List<Block> blockchain = new ArrayList<>();

        // Variabilele pentru consens PBFT
        private int view = 0;
        private int primary;
        private String pbftPhase = "idle";
        private Block currentBlock = null;
        private List<ConsensusMessage> prepareMessages = new ArrayList<>();
        private List<ConsensusMessage> commitMessages = new ArrayList<>();

        @Value("${server.port}")
        private int port;

        // Citim configurația din application.properties sau din variabile de mediu
        @Value("${NODE_ID:0}")
        private int NODE_ID;

        // Lista de noduri; de exemplu, "http://localhost:3000,http://localhost:3001,http://localhost:3002"
        @Value("${NODES:http://localhost:3000,http://localhost:3001,http://localhost:3002}")
        private String nodesStr;
        private List<String> NODES = new ArrayList<>();

        private final RestTemplate restTemplate = new RestTemplate();

        @PostConstruct
        public void init() {
            // Inițializare lista de noduri
            String[] nodesArray = nodesStr.split(",");
            for (String url : nodesArray) {
                NODES.add(url.trim());
            }
            primary = view % NODES.size();
        }

        // Endpoint pentru voturi
        @PostMapping("/vote")
        public ResponseEntity<?> vote(@RequestBody Map<String, Object> payload) {
            String vote = payload.get("vote").toString();
            System.out.println("Node " + NODE_ID + " received vote: " + vote);

            // Dacă nodul este primarul, inițiază consensul
            if (NODE_ID == primary) {
                String previousHash = blockchain.isEmpty() ? "0" : blockchain.get(blockchain.size() - 1).getHash();
                List<String> votes = new ArrayList<>();
                votes.add(vote);
                Block newBlock = new Block();
                newBlock.setIndex(blockchain.size());
                newBlock.setTimestamp(System.currentTimeMillis());
                newBlock.setVotes(votes);
                newBlock.setPreviousHash(previousHash);
                newBlock.setHash(Block.calculateHash(newBlock));
                currentBlock = newBlock;
                pbftPhase = "pre-prepare";

                // Transmite mesajul pre-prepare către celelalte noduri
                broadcastConsensusMessage("pre-prepare", currentBlock);
                return ResponseEntity.ok(Map.of("message", "Vote received and block proposal initiated by primary."));
            } else {
                // Nodurile non-primare redirecționează votul către primar
                String primaryUrl = NODES.get(primary);
                try {
                    restTemplate.postForObject(primaryUrl + "/vote", payload, String.class);
                    return ResponseEntity.ok(Map.of("message", "Vote forwarded to primary."));
                } catch (Exception ex) {
                    return ResponseEntity.status(500).body(Map.of("error", "Error forwarding vote."));
                }
            }
        }

        // Endpoint pentru a primi mesajele de consens -->nu e ordnea buna
    //    @PostMapping("/consensus")
    //    public ResponseEntity<?> consensus(@RequestBody ConsensusMessage message) {
    //        try {
    //            Thread.sleep(8000);  // 8 secunde
    //        } catch (InterruptedException e) {
    //            e.printStackTrace();
    //        }
    //        System.out.println("Node " + NODE_ID + " received a '" + message.getType() + "' message from Node " + message.getSender());
    //
    //        switch (message.getType()) {
    //            case "pre-prepare":
    //                currentBlock = message.getBlock();
    //                pbftPhase = "prepare";
    //                broadcastConsensusMessage("prepare", currentBlock);
    //                break;
    //            case "prepare":
    //                prepareMessages.add(message);
    //                if (prepareMessages.size() >= Math.floor((double) NODES.size() * 2 / 3)) {
    //                    pbftPhase = "commit";
    //                    broadcastConsensusMessage("commit", currentBlock);
    //                }
    //                break;
    //            case "commit":
    //                commitMessages.add(message);
    //                if (commitMessages.size() >= Math.floor((double) NODES.size() * 2 / 3)) {
    //                    blockchain.add(currentBlock);
    //                    System.out.println("Node " + NODE_ID + " committed block: " + currentBlock);
    //                    pbftPhase = "idle";
    //                    currentBlock = null;
    //                    prepareMessages.clear();
    //                    commitMessages.clear();
    //                }
    //                break;
    //            default:
    //                break;
    //        }
    //        return ResponseEntity.ok(Map.of("message", "Consensus message processed."));
    //    }


    //    @PostMapping("/consensus")  -->nu face commit
    //    public ResponseEntity<?> consensus(@RequestBody ConsensusMessage message) {
    ////        try {
    ////            Thread.sleep(3000);  // 8 secunde
    ////        } catch (InterruptedException e) {
    ////            e.printStackTrace();
    ////        }
    //        System.out.println("Node " + NODE_ID + " received a '" + message.getType() + "' message from Node " + message.getSender());
    //
    //        switch (message.getType()) {
    //            case "pre-prepare":
    //                if (!pbftPhase.equals("idle")) break; // Ignoră dacă nu e în faza corectă
    //                currentBlock = message.getBlock();
    //                pbftPhase = "prepare";
    //                broadcastConsensusMessage("prepare", currentBlock);
    //                break;
    //
    //            case "prepare":
    //                if (!pbftPhase.equals("prepare")) break; // Ignoră mesajele prepare înainte de pre-prepare
    //                prepareMessages.add(message);
    //                if (prepareMessages.size() >= Math.floor((double) NODES.size() * 2 / 3)) {
    //                    pbftPhase = "commit";
    //                    broadcastConsensusMessage("commit", currentBlock);
    //                }
    //                break;
    //
    //            case "commit":
    //                if (!pbftPhase.equals("commit")) break; // Ignoră mesajele commit dacă nu suntem în faza corectă
    //                commitMessages.add(message);
    //                if (commitMessages.size() >= Math.floor((double) NODES.size() * 2 / 3)) {
    //                    blockchain.add(currentBlock);
    //                    System.out.println("Node " + NODE_ID + " committed block: " + currentBlock);
    //                    pbftPhase = "idle";
    //                    currentBlock = null;
    //                    prepareMessages.clear();
    //                    commitMessages.clear();
    //                }
    //                break;
    //
    //            default:
    //                break;
    //        }
    //        return ResponseEntity.ok(Map.of("message", "Consensus message processed."));
    //    }

    //    @PostMapping("/consensus") -->nu face commit
    //    public ResponseEntity<?> consensus(@RequestBody ConsensusMessage message) {
    //        System.out.println("Node " + NODE_ID + " received a '" + message.getType() + "' message from Node " + message.getSender());
    //
    //        switch (message.getType()) {
    //            case "pre-prepare":
    //                if (!pbftPhase.equals("idle")) break;
    //                currentBlock = message.getBlock();
    //                pbftPhase = "prepare";
    //                broadcastConsensusMessage("prepare", currentBlock);
    //
    //                // Procesăm mesaje prepare care au sosit prematur
    //                synchronized (prepareMessages) {
    //                    List<ConsensusMessage> tempMessages = new ArrayList<>(prepareMessages);
    //                    prepareMessages.clear(); // Ștergem elementele din lista originală pentru a evita modificarea în timpul iterării
    //                    for (ConsensusMessage msg : tempMessages) {
    //                        processPrepareMessage(msg);
    //                    }
    //                }
    //
    //                break;
    //
    //            case "prepare":
    //                if (!pbftPhase.equals("prepare")) {
    //                    prepareMessages.add(message); // Stocăm mesajul pentru mai târziu
    //                    break;
    //                }
    //                processPrepareMessage(message);
    //                break;
    //
    //            case "commit":
    //                if (!pbftPhase.equals("commit")) {
    //                    commitMessages.add(message); // Stocăm mesajul pentru mai târziu
    //                    break;
    //                }
    //                processCommitMessage(message);
    //                break;
    //
    //            default:
    //                break;
    //        }
    //        return ResponseEntity.ok(Map.of("message", "Consensus message processed."));
    //    }
    //
    //    private void processPrepareMessage(ConsensusMessage message) {
    //        prepareMessages.add(message);
    //        if (prepareMessages.size() >= Math.floor((double) NODES.size() * 2 / 3)) {
    //            pbftPhase = "commit";
    //            broadcastConsensusMessage("commit", currentBlock);
    //
    //            // Procesăm mesaje commit care au sosit prematur
    //            synchronized (commitMessages) {
    //                Iterator<ConsensusMessage> iterator = commitMessages.iterator();
    //                while (iterator.hasNext()) {
    //                    processCommitMessage(iterator.next());
    //                    iterator.remove();
    //                }
    //            }
    //        }
    //    }
    //
    //    private void processCommitMessage(ConsensusMessage message) {
    //        commitMessages.add(message);
    //        if (commitMessages.size() >= Math.floor((double) NODES.size() * 2 / 3)) {
    //            blockchain.add(currentBlock);
    //            System.out.println("Node " + NODE_ID + " committed block: " + currentBlock);
    //            pbftPhase = "idle";
    //            currentBlock = null;
    //            prepareMessages.clear();
    //            commitMessages.clear();
    //        }
    //    }


        @PostMapping("/consensus")
        public ResponseEntity<?> consensus(@RequestBody ConsensusMessage message) {
            System.out.println("Node " + NODE_ID + " received a '" + message.getType() + "' message from Node " + message.getSender());

            switch (message.getType()) {
                case "pre-prepare":
                    if (!pbftPhase.equals("idle")) break;
                    currentBlock = message.getBlock();
                    pbftPhase = "prepare";

                    broadcastConsensusMessage("prepare", currentBlock);

                    // Procesăm mesaje prepare care au sosit prematur
                    synchronized (prepareMessages) {
                        List<ConsensusMessage> tempMessages = new ArrayList<>(prepareMessages);
                        prepareMessages.clear(); // Ștergem elementele din lista originală pentru a evita modificarea în timpul iterării
                        for (ConsensusMessage msg : tempMessages) {
                            processPrepareMessage(msg);
                        }
                    }

                    break;

                case "prepare":
                    if (!pbftPhase.equals("prepare")) {
                        prepareMessages.add(message); // Stocăm mesajul pentru mai târziu
                        break;
                    }
                    processPrepareMessage(message);
                    break;

                case "commit":
                    if (!pbftPhase.equals("commit")) {
                        commitMessages.add(message); // Stocăm mesajul pentru mai târziu
                        break;
                    }
                    processCommitMessage(message);
                    break;

                default:
                    break;
            }
            return ResponseEntity.ok(Map.of("message", "Consensus message processed."));
        }

        private void processPrepareMessage(ConsensusMessage message) {
            prepareMessages.add(message);

            System.out.println("Node " + NODE_ID + " stored prepare message. Count: " + prepareMessages.size());


            if (prepareMessages.size() >=1){           // Math.floor((double) NODES.size() * 2 / 3)) {
                pbftPhase = "commit";

                broadcastConsensusMessage("commit", currentBlock);

                // Procesăm mesaje commit care au sosit prematur
                synchronized (commitMessages) {
                    List<ConsensusMessage> commitMessagesCopy = new ArrayList<>(commitMessages);
                    commitMessages.clear(); // Curățăm lista înainte de iterare
                    for (ConsensusMessage msg : commitMessagesCopy) {
                        processCommitMessage(msg);
                    }
                }
            }
        }


        private void processCommitMessage(ConsensusMessage message) {
            commitMessages.add(message);

            System.out.println("Node " + NODE_ID + " stored commit message. Count: " + commitMessages.size());


            if (commitMessages.size() >=Math.floor((double) NODES.size() * 2 / 3)) {
                blockchain.add(currentBlock);
                System.out.println("Node " + NODE_ID + " committed block: " + currentBlock);
                pbftPhase = "idle";
                currentBlock = null;

                // Curățăm listele de mesaje
                prepareMessages.clear();
                commitMessages.clear();
            }
        }









        // Endpoint pentru a vizualiza blockchain-ul
        @GetMapping("/chain")
        public ResponseEntity<List<Block>> getChain() {
            return ResponseEntity.ok(blockchain);
        }

        // Utilitar pentru a transmite mesajele de consens către celelalte noduri
        private void broadcastConsensusMessage(String type, Block block) {
            System.out.println("Node " + NODE_ID + " broadcasting '" + type + "' message");

            ConsensusMessage message = new ConsensusMessage();
            message.setType(type);
            message.setBlock(block);
            message.setSender(NODE_ID);
            for (String nodeUrl : NODES) {
                // Nu trimitem mesajul către noi înșine
                if (!nodeUrl.contains(":" + port)) { // poți adapta condiția pentru a identifica nodul curent
                    try {
                        restTemplate.postForObject(nodeUrl + "/consensus", message, String.class);
                    } catch (Exception ex) {
                        System.err.println("Error sending message to " + nodeUrl + ": " + ex.getMessage());
                    }
                }
            }
        }
    }