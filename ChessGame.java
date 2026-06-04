//Game developed by 
// Iqra Attique
// Maimona Mushtaq
// Bisma Amir

import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;   
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.*;

public class ChessGame {
    private JFrame frame;
    private JPanel boardPanel;
    private JButton[][] tiles = new JButton[8][8];
    private ChessPiece[][] board = new ChessPiece[8][8];
    private boolean whiteTurn = true;
    private Point selected = null;
    private Set<Point> legalMoves = new HashSet<>();
    private Theme currentTheme = Theme.CLASSIC;
    private boolean gameOver = false;
    private String currentPlayerName = "Player";
    private JLabel statusLabel;
    
    private List<ChessPiece> whiteCaptured = new ArrayList<>();
    private List<ChessPiece> blackCaptured = new ArrayList<>();
    
    private JPanel whiteCapturedPanel;
    private JPanel blackCapturedPanel;
    private JLabel whiteCapturedDetails;
    private JLabel blackCapturedDetails;
    
    private int whiteTime = 15 * 60;
    private int blackTime = 15 * 60;
    private Timer gameTimer;
    private JLabel whiteTimerLabel;
    private JLabel blackTimerLabel;

    enum Theme {
        CLASSIC(new Color(240, 217, 181), new Color(181, 136, 99)),
        DARK(new Color(60, 60, 60), new Color(30, 30, 30)),
        PINK(new Color(255, 220, 230), new Color(230, 180, 190));

        final Color light, dark;
        Theme(Color l, Color d) {
            this.light = l;
            this.dark = d;
        }

        @Override
        public String toString() {
            String n = name();
            return n.charAt(0) + n.substring(1).toLowerCase();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ChessGame().start());
    }

    void start() {
        frame = new JFrame("Chess Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Status panel
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusLabel = new JLabel("WHITE'S TURN", SwingConstants.CENTER);
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        statusLabel.setForeground(Color.BLUE);
        statusPanel.add(statusLabel, BorderLayout.CENTER);

        JPanel resignPanel = new JPanel(new FlowLayout());
        JButton whiteResign = new JButton("White Resigns");
        JButton blackResign = new JButton("Black Resigns");
        
        whiteResign.addActionListener(e -> resign(true));
        blackResign.addActionListener(e -> resign(false));
        
        resignPanel.add(whiteResign);
        resignPanel.add(blackResign);
        statusPanel.add(resignPanel, BorderLayout.EAST);

        frame.add(statusPanel, BorderLayout.NORTH);

        // Main container
        JPanel mainContainer = new JPanel(new BorderLayout());
        mainContainer.setBackground(Color.WHITE);

        // LEFT SIDE: Black captured pieces (White ne capture kiye)
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setPreferredSize(new Dimension(150, 600));
        leftPanel.setBackground(new Color(240, 240, 240));
        
        blackCapturedPanel = new JPanel();
        blackCapturedPanel.setLayout(new BoxLayout(blackCapturedPanel, BoxLayout.Y_AXIS));
        blackCapturedPanel.setBackground(new Color(240, 240, 240));
        blackCapturedPanel.setBorder(BorderFactory.createTitledBorder("CAPTURED BY WHITE"));
        
        blackCapturedDetails = new JLabel("No pieces captured");
        blackCapturedDetails.setFont(new Font("SansSerif", Font.PLAIN, 12));
        blackCapturedDetails.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        JScrollPane blackScroll = new JScrollPane(blackCapturedPanel);
        blackScroll.setPreferredSize(new Dimension(140, 250));
        
        leftPanel.add(blackCapturedDetails, BorderLayout.NORTH);
        leftPanel.add(blackScroll, BorderLayout.CENTER);
        
        JPanel leftTimerPanel = new JPanel(new FlowLayout());
        leftTimerPanel.setBackground(new Color(240, 240, 240));
        JLabel blackTimerText = new JLabel("BLACK: ");
        blackTimerText.setFont(new Font("SansSerif", Font.BOLD, 14));
        blackTimerLabel = new JLabel(formatTime(blackTime));
        blackTimerLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        blackTimerLabel.setForeground(Color.BLACK);
        leftTimerPanel.add(blackTimerText);
        leftTimerPanel.add(blackTimerLabel);
        leftPanel.add(leftTimerPanel, BorderLayout.SOUTH);

        mainContainer.add(leftPanel, BorderLayout.WEST);

        // CENTER: Chess board
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(Color.WHITE);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel blackLabel = new JLabel("BLACK", SwingConstants.CENTER);
        blackLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        blackLabel.setForeground(Color.DARK_GRAY);
        blackLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));
        centerPanel.add(blackLabel, BorderLayout.NORTH);

        boardPanel = new JPanel(new GridLayout(8, 8, 2, 2));
        boardPanel.setBackground(Color.WHITE);
        boardPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 2),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        centerPanel.add(boardPanel, BorderLayout.CENTER);

        JLabel whiteLabel = new JLabel("WHITE", SwingConstants.CENTER);
        whiteLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        whiteLabel.setForeground(Color.DARK_GRAY);
        whiteLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));
        centerPanel.add(whiteLabel, BorderLayout.SOUTH);

        mainContainer.add(centerPanel, BorderLayout.CENTER);

        // RIGHT SIDE: White captured pieces (Black ne capture kiye)
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setPreferredSize(new Dimension(150, 600));
        rightPanel.setBackground(new Color(240, 240, 240));
        
        whiteCapturedPanel = new JPanel();
        whiteCapturedPanel.setLayout(new BoxLayout(whiteCapturedPanel, BoxLayout.Y_AXIS));
        whiteCapturedPanel.setBackground(new Color(240, 240, 240));
        whiteCapturedPanel.setBorder(BorderFactory.createTitledBorder("CAPTURED BY BLACK"));
        
        whiteCapturedDetails = new JLabel("No pieces captured");
        whiteCapturedDetails.setFont(new Font("SansSerif", Font.PLAIN, 12));
        whiteCapturedDetails.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        JScrollPane whiteScroll = new JScrollPane(whiteCapturedPanel);
        whiteScroll.setPreferredSize(new Dimension(140, 250));
        
        rightPanel.add(whiteCapturedDetails, BorderLayout.NORTH);
        rightPanel.add(whiteScroll, BorderLayout.CENTER);
        
        JPanel rightTimerPanel = new JPanel(new FlowLayout());
        rightTimerPanel.setBackground(new Color(240, 240, 240));
        JLabel whiteTimerText = new JLabel("WHITE: ");
        whiteTimerText.setFont(new Font("SansSerif", Font.BOLD, 14));
        whiteTimerLabel = new JLabel(formatTime(whiteTime));
        whiteTimerLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        whiteTimerLabel.setForeground(Color.BLACK);
        rightTimerPanel.add(whiteTimerText);
        rightTimerPanel.add(whiteTimerLabel);
        rightPanel.add(rightTimerPanel, BorderLayout.SOUTH);

        mainContainer.add(rightPanel, BorderLayout.EAST);

        frame.add(mainContainer, BorderLayout.CENTER);

        frame.setJMenuBar(createMenuBar());

        initializeBoard();
        redrawBoard();
        updateStatus();
        updateCapturedPanels();
        startTimer();

        frame.setSize(900, 750);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void startTimer() {
        gameTimer = new Timer(true);
        gameTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> {
                    if (!gameOver) {
                        if (whiteTurn) {
                            whiteTime--;
                            if (whiteTime <= 0) {
                                whiteTime = 0;
                                timeOut(true);
                            }
                        } else {
                            blackTime--;
                            if (blackTime <= 0) {
                                blackTime = 0;
                                timeOut(false);
                            }
                        }
                        updateTimerDisplay();
                    }
                });
            }
        }, 1000, 1000);
    }

    private void timeOut(boolean whiteTimedOut) {
        gameOver = true;
        gameTimer.cancel();
        
        String timedOutPlayer = whiteTimedOut ? "White" : "Black";
        String winner = whiteTimedOut ? "Black" : "White";
        
        int winnerScore = calculateScoreForPlayer(!whiteTimedOut) + getCapturedScore(!whiteTimedOut);
        int loserScore = calculateScoreForPlayer(whiteTimedOut) + getCapturedScore(whiteTimedOut);
        
        try {
            SaveScore.save(currentPlayerName + " (" + winner + ")", winnerScore);
            SaveScore.save(currentPlayerName + " (" + timedOutPlayer + ")", loserScore);
        } catch (Exception e) {
            // Ignore save errors
        }
        
        JOptionPane.showMessageDialog(frame,
            timedOutPlayer + " ran out of time!\n\n" +
            "🎉 " + winner + " wins! 🎉\n\n" +
            "Final Scores:\n" +
            winner + ": " + winnerScore + " points\n" +
            timedOutPlayer + ": " + loserScore + " points",
            "Time Out!",
            JOptionPane.INFORMATION_MESSAGE);
        
        updateStatus();
    }

    private void updateTimerDisplay() {
        whiteTimerLabel.setText(formatTime(whiteTime));
        blackTimerLabel.setText(formatTime(blackTime));
        
        if (whiteTime < 60) whiteTimerLabel.setForeground(Color.RED);
        else whiteTimerLabel.setForeground(Color.BLACK);
        
        if (blackTime < 60) blackTimerLabel.setForeground(Color.RED);
        else blackTimerLabel.setForeground(Color.BLACK);
        
        if (whiteTurn) {
            whiteTimerLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
            whiteTimerLabel.setForeground(Color.BLUE);
            blackTimerLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
            blackTimerLabel.setForeground(Color.BLACK);
        } else {
            blackTimerLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
            blackTimerLabel.setForeground(Color.RED);
            whiteTimerLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
            whiteTimerLabel.setForeground(Color.BLACK);
        }
    }

    private String formatTime(int seconds) {
        int minutes = seconds / 60;
        int secs = seconds % 60;
        return String.format("%02d:%02d", minutes, secs);
    }

    private void updateCapturedPanels() {
        whiteCapturedPanel.removeAll();
        blackCapturedPanel.removeAll();

        if (blackCaptured.isEmpty()) {
            JLabel noPieces = new JLabel("No pieces captured yet");
            noPieces.setForeground(Color.GRAY);
            noPieces.setFont(new Font("SansSerif", Font.ITALIC, 12));
            blackCapturedPanel.add(noPieces);
        } else {
            java.util.Map<String, Integer> blackPieceCount = new java.util.HashMap<>();
            for (ChessPiece piece : blackCaptured) {
                String pieceName = getPieceName(piece);
                blackPieceCount.put(pieceName, blackPieceCount.getOrDefault(pieceName, 0) + 1);
            }
            
            for (java.util.Map.Entry<String, Integer> entry : blackPieceCount.entrySet()) {
                JPanel pieceRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
                pieceRow.setBackground(new Color(240, 240, 240));
                
                ChessPiece samplePiece = blackCaptured.stream()
                    .filter(p -> getPieceName(p).equals(entry.getKey()))
                    .findFirst()
                    .orElse(null);
                
                if (samplePiece != null) {
                    JLabel symbolLabel = new JLabel(samplePiece.symbol);
                    symbolLabel.setFont(new Font("Serif", Font.BOLD, 20));
                    symbolLabel.setForeground(Color.WHITE);
                    symbolLabel.setOpaque(true);
                    symbolLabel.setBackground(Color.DARK_GRAY);
                    symbolLabel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
                    
                    JLabel nameLabel = new JLabel(entry.getKey() + " x" + entry.getValue());
                    nameLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
                    
                    pieceRow.add(symbolLabel);
                    pieceRow.add(nameLabel);
                    blackCapturedPanel.add(pieceRow);
                }
            }
        }

        if (whiteCaptured.isEmpty()) {
            JLabel noPieces = new JLabel("No pieces captured yet");
            noPieces.setForeground(Color.GRAY);
            noPieces.setFont(new Font("SansSerif", Font.ITALIC, 12));
            whiteCapturedPanel.add(noPieces);
        } else {
            java.util.Map<String, Integer> whitePieceCount = new java.util.HashMap<>();
            for (ChessPiece piece : whiteCaptured) {
                String pieceName = getPieceName(piece);
                whitePieceCount.put(pieceName, whitePieceCount.getOrDefault(pieceName, 0) + 1);
            }
            
            for (java.util.Map.Entry<String, Integer> entry : whitePieceCount.entrySet()) {
                JPanel pieceRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
                pieceRow.setBackground(new Color(240, 240, 240));
                
                ChessPiece samplePiece = whiteCaptured.stream()
                    .filter(p -> getPieceName(p).equals(entry.getKey()))
                    .findFirst()
                    .orElse(null);
                
                if (samplePiece != null) {
                    JLabel symbolLabel = new JLabel(samplePiece.symbol);
                    symbolLabel.setFont(new Font("Serif", Font.BOLD, 20));
                    symbolLabel.setForeground(Color.BLACK);
                    symbolLabel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
                    
                    JLabel nameLabel = new JLabel(entry.getKey() + " x" + entry.getValue());
                    nameLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
                    
                    pieceRow.add(symbolLabel);
                    pieceRow.add(nameLabel);
                    whiteCapturedPanel.add(pieceRow);
                }
            }
        }

        whiteCapturedDetails.setText("Total: " + blackCaptured.size() + " pieces");
        blackCapturedDetails.setText("Total: " + whiteCaptured.size() + " pieces");

        whiteCapturedPanel.revalidate();
        whiteCapturedPanel.repaint();
        blackCapturedPanel.revalidate();
        blackCapturedPanel.repaint();
    }

    private String getPieceName(ChessPiece piece) {
        return switch (piece.type) {
            case "p" -> "Pawn";
            case "N" -> "Knight";
            case "B" -> "Bishop";
            case "R" -> "Rook";
            case "Q" -> "Queen";
            case "K" -> "King";
            default -> "Unknown";
        };
    }

    private void addCapturedPiece(ChessPiece piece) {
        if (piece.white) {
            whiteCaptured.add(piece);
        } else {
            blackCaptured.add(piece);
        }
        updateCapturedPanels();
    }

    private int getCapturedScore(boolean forWhite) {
        int score = 0;
        List<ChessPiece> captured = forWhite ? blackCaptured : whiteCaptured;
        
        for (ChessPiece piece : captured) {
            switch (piece.type) {
                case "p" -> score += 1;
                case "N", "B" -> score += 3;
                case "R" -> score += 5;
                case "Q" -> score += 9;
            }
        }
        return score;
    }

    // ✅ FIXED: Turn display
    private void updateStatus() {
        if (gameOver) {
            statusLabel.setText("GAME OVER");
            statusLabel.setForeground(Color.RED);
        } else {
            if (whiteTurn) {
                statusLabel.setText("BLACK'S TURN - Black should move now");
                statusLabel.setForeground(Color.BLUE);
            } else {
                statusLabel.setText("WHITE'S TURN - white should move now");
                statusLabel.setForeground(Color.RED);
            }
        }
    }

    private void resign(boolean whiteResigning) {
        if (gameOver) return;

        String resigningPlayer = whiteResigning ? "White" : "Black";
        String winningPlayer = whiteResigning ? "Black" : "White";
        
        int result = JOptionPane.showConfirmDialog(frame,
            resigningPlayer + ", are you sure you want to resign?\n" +
            winningPlayer + " will win the game.",
            "Confirm Resignation",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
            gameOver = true;
            if (gameTimer != null) {
                gameTimer.cancel();
            }
            
            int winnerScore = calculateScoreForPlayer(!whiteResigning) + getCapturedScore(!whiteResigning);
            int loserScore = calculateScoreForPlayer(whiteResigning) + getCapturedScore(whiteResigning);
            
            String winnerName = currentPlayerName + " (" + winningPlayer + ")";
            String loserName = currentPlayerName + " (" + resigningPlayer + ")";
            
            try {
                SaveScore.save(winnerName, winnerScore);
                SaveScore.save(loserName, loserScore);
            } catch (Exception e) {
                // Ignore save errors
            }
            
            JOptionPane.showMessageDialog(frame,
                resigningPlayer + " has resigned!\n\n" +
                "🎉 " + winningPlayer + " wins! 🎉\n\n" +
                "Captured Pieces:\n" +
                "White captured: " + blackCaptured.size() + " pieces\n" +
                "Black captured: " + whiteCaptured.size() + " pieces\n\n" +
                "Final Scores:\n" +
                winningPlayer + ": " + winnerScore + " points\n" +
                resigningPlayer + ": " + loserScore + " points",
                "Game Over - Resignation",
                JOptionPane.INFORMATION_MESSAGE);
            
            updateStatus();
        }
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu gameMenu = new JMenu("Game");
        JMenuItem newGame = new JMenuItem("New Game");
        newGame.addActionListener(e -> resetGame());
        gameMenu.add(newGame);

        JMenuItem saveScore = new JMenuItem("Save Current Score");
        saveScore.addActionListener(e -> saveCurrentScore());
        gameMenu.add(saveScore);

        JMenuItem showScores = new JMenuItem("Show High Scores");
        showScores.addActionListener(e -> showHighScores());
        gameMenu.add(showScores);

        JMenuItem setPlayer = new JMenuItem("Set Player Name");
        setPlayer.addActionListener(e -> setPlayerName());
        gameMenu.add(setPlayer);

        JMenuItem resign = new JMenuItem("Resign Game");
        resign.addActionListener(e -> resign(whiteTurn));
        gameMenu.add(resign);

        JMenuItem quitGame = new JMenuItem("Quit and Save");
        quitGame.addActionListener(e -> quitAndSave());
        gameMenu.add(quitGame);

        JMenu themeMenu = new JMenu("Theme");
        for (Theme theme : Theme.values()) {
            JMenuItem item = new JMenuItem(theme.toString());
            item.addActionListener(e -> {
                currentTheme = theme;
                redrawBoard();
            });
            themeMenu.add(item);
        }

        menuBar.add(gameMenu);
        menuBar.add(themeMenu);
        return menuBar;
    }

    private void quitAndSave() {
        if (gameOver) {
            if (gameTimer != null) {
                gameTimer.cancel();
            }
            frame.dispose();
            return;
        }

        int result = JOptionPane.showConfirmDialog(frame,
            "Are you sure you want to quit?\nCurrent game progress will be saved.\n\n" +
            "Captured Pieces:\n" +
            "White: " + blackCaptured.size() + " pieces\n" +
            "Black: " + whiteCaptured.size() + " pieces",
            "Quit Game",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
            if (gameTimer != null) {
                gameTimer.cancel();
            }
            
            int whiteScore = calculateScoreForPlayer(true) + getCapturedScore(true);
            int blackScore = calculateScoreForPlayer(false) + getCapturedScore(false);
            
            String whitePlayerName = currentPlayerName + " (White)";
            String blackPlayerName = currentPlayerName + " (Black)";
            
            try {
                SaveScore.save(whitePlayerName, whiteScore);
                SaveScore.save(blackPlayerName, blackScore);
                
                JOptionPane.showMessageDialog(frame,
                    "Game saved successfully!\n\n" +
                    "Captured Pieces:\n" +
                    "White: " + blackCaptured.size() + " pieces\n" +
                    "Black: " + whiteCaptured.size() + " pieces\n\n" +
                    "Final Scores:\n" +
                    "White: " + whiteScore + " points\n" +
                    "Black: " + blackScore + " points\n\n" +
                    "Thank you for playing!",
                    "Game Saved",
                    JOptionPane.INFORMATION_MESSAGE);
                    
            } catch (Exception e) {
                JOptionPane.showMessageDialog(frame,
                    "Error saving scores: " + e.getMessage(),
                    "Save Error",
                    JOptionPane.ERROR_MESSAGE);
            }
            
            frame.dispose();
        }
    }

    private int calculateScoreForPlayer(boolean isWhite) {
        int score = 0;
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                ChessPiece piece = board[r][c];
                if (piece != null && piece.white == isWhite) {
                    switch (piece.type) {
                        case "p" -> score += 1;
                        case "N", "B" -> score += 3;
                        case "R" -> score += 5;
                        case "Q" -> score += 9;
                        case "K" -> score += 0;
                    }
                }
            }
        }
        return score;
    }

    private void saveCurrentScore() {
        if (currentPlayerName.equals("Player")) {
            setPlayerName();
        }
        
        int whiteScore = calculateScoreForPlayer(true) + getCapturedScore(true);
        int blackScore = calculateScoreForPlayer(false) + getCapturedScore(false);
        
        try {
            SaveScore.save(currentPlayerName + " (White)", whiteScore);
            SaveScore.save(currentPlayerName + " (Black)", blackScore);
            JOptionPane.showMessageDialog(frame, 
                "Scores saved successfully!\n\n" +
                "White: " + whiteScore + " points\n" +
                "Black: " + blackScore + " points\n\n" +
                "Captured Pieces:\n" +
                "White: " + blackCaptured.size() + " pieces\n" +
                "Black: " + whiteCaptured.size() + " pieces", 
                "Scores Saved", 
                JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, 
                "Error saving score: " + e.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private int calculateScore() {
        return calculateScoreForPlayer(true) + calculateScoreForPlayer(false) +
               getCapturedScore(true) + getCapturedScore(false);
    }

    private String padRight(String s, int n) {
        return String.format("%-" + n + "s", s);
    }

    private void showHighScores() {
        try {
            StringBuilder scores = new StringBuilder("🏆 High Scores 🏆\n\n");
            scores.append(padRight("Player Name", 20)).append("Score").append("\n");
            scores.append("--------------------    -----").append("\n");
            
            Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/lab", 
                "root", 
                "root123"  // Apna password yahan dalen
            );
            
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(
                "SELECT player_name, score FROM scores ORDER BY score DESC LIMIT 10"
            );
            
            int rank = 1;
            boolean hasScores = false;
            
            while(rs.next()) {
                hasScores = true;
                String playerName = rs.getString("player_name");
                int score = rs.getInt("score");
                scores.append(rank).append(". ").append(padRight(playerName, 18))
                      .append("    ").append(score).append("\n");
                rank++;
            }
            
            conn.close();
            
            if (!hasScores) {
                scores.append("No scores saved yet!\n");
            }
            
            JOptionPane.showMessageDialog(frame, scores.toString(), 
                "High Scores", JOptionPane.INFORMATION_MESSAGE);
                
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, 
                "Error loading scores: " + e.getMessage(), 
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void setPlayerName() {
        String name = JOptionPane.showInputDialog(frame, 
            "Enter your player name:", 
            "Player Name", 
            JOptionPane.QUESTION_MESSAGE);
            
        if (name != null && !name.trim().isEmpty()) {
            currentPlayerName = name.trim();
            JOptionPane.showMessageDialog(frame, 
                "Player name set to: " + currentPlayerName, 
                "Success", 
                JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void resetGame() {
        if (gameTimer != null) {
            gameTimer.cancel();
        }
        initializeBoard();
        gameOver = false;
        whiteTurn = true; // Start with White's turn
        selected = null;
        legalMoves.clear();
        whiteCaptured.clear();
        blackCaptured.clear();
        whiteTime = 15 * 60;
        blackTime = 15 * 60;
        redrawBoard();
        updateStatus();
        updateCapturedPanels();
        updateTimerDisplay();
        startTimer();
    }

    private void initializeBoard() {
        for (int r = 0; r < 8; r++)
            for (int c = 0; c < 8; c++)
                board[r][c] = null;

        // Black pieces (top) - row 0
        board[0][0] = new ChessPiece("bR");
        board[0][1] = new ChessPiece("bN");
        board[0][2] = new ChessPiece("bB");
        board[0][3] = new ChessPiece("bQ");
        board[0][4] = new ChessPiece("bK");
        board[0][5] = new ChessPiece("bB");
        board[0][6] = new ChessPiece("bN");
        board[0][7] = new ChessPiece("bR");
        
        for (int c = 0; c < 8; c++) {
            board[1][c] = new ChessPiece("bp");
        }

        // White pieces (bottom) - row 7
        board[7][0] = new ChessPiece("wR");
        board[7][1] = new ChessPiece("wN");
        board[7][2] = new ChessPiece("wB");
        board[7][3] = new ChessPiece("wQ");
        board[7][4] = new ChessPiece("wK");
        board[7][5] = new ChessPiece("wB");
        board[7][6] = new ChessPiece("wN");
        board[7][7] = new ChessPiece("wR");
        
        for (int c = 0; c < 8; c++) {
            board[6][c] = new ChessPiece("wp");
        }
    }

    private void redrawBoard() {
        boardPanel.removeAll();
        for (int r = 7; r >= 0; r--) {
            for (int c = 0; c < 8; c++) {
                JButton btn = tiles[r][c];
                if (btn == null) {
                    btn = new JButton();
                    btn.setFont(new Font("Serif", Font.BOLD, 36));
                    btn.setMargin(new Insets(0, 0, 0, 0));
                    btn.setFocusPainted(false);
                    btn.setOpaque(true);
                    btn.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true));
                    final int rr = r, cc = c;
                    btn.addActionListener(e -> handleClick(rr, cc));
                    tiles[r][c] = btn;
                }

                Color baseColor = (r + c) % 2 == 0 ? currentTheme.light : currentTheme.dark;
                btn.setBackground(baseColor);

                if (selected != null && selected.x == r && selected.y == c) {
                    btn.setBackground(new Color(166, 219, 160));
                } else if (legalMoves.contains(new Point(r, c))) {
                    btn.setBackground(new Color(147, 197, 253));
                }

                ChessPiece p = board[r][c];
                if (p != null) {
                    btn.setText(p.symbol);
                    btn.setForeground(p.white ? Color.BLACK : Color.WHITE);
                } else {
                    btn.setText("");
                }

                boardPanel.add(btn);
            }
        }
        boardPanel.revalidate();
        boardPanel.repaint();
    }

    private void handleClick(int r, int c) {
        if (gameOver) return;

        ChessPiece clicked = board[r][c];
        if (selected == null) {
            if (clicked != null && clicked.white == whiteTurn) {
                selected = new Point(r, c);
                legalMoves = calculateLegalMoves(r, c);
                redrawBoard();
            }
        } else {
            Point move = new Point(r, c);
            if (legalMoves.contains(move)) {
                ChessPiece movedPiece = board[selected.x][selected.y];
                ChessPiece targetPiece = board[r][c];
                
                if (targetPiece != null) {
                    addCapturedPiece(targetPiece);
                }
                
                board[r][c] = movedPiece;
                board[selected.x][selected.y] = null;

                if (isKingInCheck(movedPiece.white)) {
                    board[selected.x][selected.y] = movedPiece;
                    board[r][c] = targetPiece;
                    if (targetPiece != null) {
                        if (targetPiece.white) {
                            whiteCaptured.remove(whiteCaptured.size() - 1);
                        } else {
                            blackCaptured.remove(blackCaptured.size() - 1);
                        }
                        updateCapturedPanels();
                    }
                    JOptionPane.showMessageDialog(frame, "Illegal move: your king would be in check!", "Invalid Move", JOptionPane.WARNING_MESSAGE);
                } else {
                    // ✅ FIXED: Turn switch after successful move
                    whiteTurn = !whiteTurn;
                    updateStatus();
                    updateTimerDisplay();
                    
                    boolean opponentInCheck = isKingInCheck(whiteTurn);
                    boolean opponentCheckmate = opponentInCheck && noLegalMoves(whiteTurn);
                    boolean stalemate = !opponentInCheck && noLegalMoves(whiteTurn);

                    if (opponentCheckmate) {
                        gameOver = true;
                        if (gameTimer != null) {
                            gameTimer.cancel();
                        }
                        String winner = whiteTurn ? "Black" : "White";
                        String loser = whiteTurn ? "White" : "Black";
                        
                        int winnerScore = calculateScoreForPlayer(!whiteTurn) + getCapturedScore(!whiteTurn);
                        int loserScore = calculateScoreForPlayer(whiteTurn) + getCapturedScore(whiteTurn);
                        
                        try {
                            SaveScore.save(currentPlayerName + " (" + winner + ")", winnerScore);
                            SaveScore.save(currentPlayerName + " (" + loser + ")", loserScore);
                        } catch (Exception e) {
                            // Ignore save errors
                        }
                        
                        JOptionPane.showMessageDialog(frame, 
                            loser + " is checkmated. " + winner + " wins!\n\n" +
                            "Captured Pieces:\n" +
                            "White: " + blackCaptured.size() + " pieces\n" +
                            "Black: " + whiteCaptured.size() + " pieces\n\n" +
                            "Final Scores:\n" +
                            winner + ": " + winnerScore + " points\n" +
                            loser + ": " + loserScore + " points", 
                            "Game Over", 
                            JOptionPane.INFORMATION_MESSAGE);
                        updateStatus();
                    } else if (stalemate) {
                        gameOver = true;
                        if (gameTimer != null) {
                            gameTimer.cancel();
                        }
                        int whiteScore = calculateScoreForPlayer(true) + getCapturedScore(true);
                        int blackScore = calculateScoreForPlayer(false) + getCapturedScore(false);
                        
                        try {
                            SaveScore.save(currentPlayerName + " (White)", whiteScore);
                            SaveScore.save(currentPlayerName + " (Black)", blackScore);
                        } catch (Exception e) {
                            // Ignore save errors
                        }
                        
                        JOptionPane.showMessageDialog(frame, 
                            "Stalemate! The game is a draw.\n\n" +
                            "Captured Pieces:\n" +
                            "White: " + blackCaptured.size() + " pieces\n" +
                            "Black: " + whiteCaptured.size() + " pieces\n\n" +
                            "Final Scores:\n" +
                            "White: " + whiteScore + " points\n" +
                            "Black: " + blackScore + " points", 
                            "Game Over", 
                            JOptionPane.INFORMATION_MESSAGE);
                        updateStatus();
                    }
                }
            }
            selected = null;
            legalMoves.clear();
            redrawBoard();
        }
    }

    private boolean noLegalMoves(boolean white) {
        for (int r = 0; r < 8; r++)
            for (int c = 0; c < 8; c++) {
                ChessPiece p = board[r][c];
                if (p != null && p.white == white && !calculateLegalMoves(r, c).isEmpty()) return false;
            }
        return true;
    }

    private boolean isKingInCheck(boolean white) {
        Point kingPos = findKing(white);
        if (kingPos == null) return false;
        for (int r = 0; r < 8; r++)
            for (int c = 0; c < 8; c++) {
                ChessPiece attacker = board[r][c];
                if (attacker != null && attacker.white != white) {
                    if (validMove(r, c, kingPos.x, kingPos.y)) return true;
                }
            }
        return false;
    }

    private Point findKing(boolean white) {
        for (int r = 0; r < 8; r++)
            for (int c = 0; c < 8; c++) {
                ChessPiece p = board[r][c];
                if (p != null && p.white == white && p.type.equals("K"))
                    return new Point(r, c);
            }
        return null;
    }

    private Set<Point> calculateLegalMoves(int fr, int fc) {
        Set<Point> moves = new HashSet<>();
        ChessPiece p = board[fr][fc];
        if (p == null) return moves;

        for (int tr = 0; tr < 8; tr++)
            for (int tc = 0; tc < 8; tc++)
                if (validMove(fr, fc, tr, tc)) {
                    ChessPiece origDest = board[tr][tc];
                    board[tr][tc] = p;
                    board[fr][fc] = null;
                    if (!isKingInCheck(p.white)) moves.add(new Point(tr, tc));
                    board[fr][fc] = p;
                    board[tr][tc] = origDest;
                }
        return moves;
    }

    // ✅ FIXED: King movement
    private boolean validMove(int fr, int fc, int tr, int tc) {
        if (fr == tr && fc == tc) return false;
        ChessPiece p = board[fr][fc], target = board[tr][tc];
        if (p == null || (target != null && target.white == p.white)) return false;

        int dr = tr - fr, dc = tc - fc;
        int adr = Math.abs(dr), adc = Math.abs(dc);

        return switch (p.type) {
            case "p" -> {
                int dir = p.white ? -1 : 1;
                boolean single = dr == dir && dc == 0 && target == null;
                boolean doubleStep = ((fr == 6 && p.white) || (fr == 1 && !p.white)) && dr == 2 * dir && dc == 0 && target == null && board[fr + dir][fc] == null;
                boolean capture = dr == dir && Math.abs(dc) == 1 && target != null && target.white != p.white;
                yield single || doubleStep || capture;
            }
            case "R" -> (fr == tr || fc == tc) && clearPath(fr, fc, tr, tc);
            case "N" -> adr * adc == 2;
            case "B" -> adr == adc && clearPath(fr, fc, tr, tc);
            case "Q" -> (fr == tr || fc == tc || adr == adc) && clearPath(fr, fc, tr, tc);
            case "K" -> {
                // ✅ FIXED: King can only move 1 square in any direction
                boolean kingMove = adr <= 1 && adc <= 1;
                yield kingMove;
            }
            default -> false;
        };
    }

    private boolean clearPath(int fr, int fc, int tr, int tc) {
        int dr = Integer.signum(tr - fr), dc = Integer.signum(tc - fc);
        int r = fr + dr, c = fc + dc;
        while (r != tr || c != tc) {
            if (board[r][c] != null) return false;
            r += dr;
            c += dc;
        }
        return true;
    }

    static class ChessPiece {
        boolean white;
        String type, symbol;

        ChessPiece(String code) {
            white = code.charAt(0) == 'w';
            type = code.substring(1);
            symbol = switch (code) {
                case "wK" -> "♔"; case "wQ" -> "♕"; case "wR" -> "♖";
                case "wB" -> "♗"; case "wN" -> "♘"; case "wp" -> "♙";
                case "bK" -> "♚"; case "bQ" -> "♛"; case "bR" -> "♜";
                case "bB" -> "♝"; case "bN" -> "♞"; case "bp" -> "♟";
                default -> "";
            };
        }
    }
}

class SaveScore {
    public static void save(String player, int score) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/lab", 
                "root", 
                "root123"  // Apna password yahan dalen
            );
            
            String query = "INSERT INTO scores(player_name, score) VALUES (?, ?)";
            PreparedStatement pst = conn.prepareStatement(query);
            pst.setString(1, player);
            pst.setInt(2, score);
            pst.executeUpdate();
            
            conn.close();
            System.out.println("✓ Score Saved to Database!");
        } catch (Exception e) {
            System.out.println("✗ Error saving score: " + e.getMessage());
            throw new RuntimeException("Failed to save score", e);
        }
    }
}