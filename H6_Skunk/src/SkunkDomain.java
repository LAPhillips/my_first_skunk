import java.util.ArrayList;
import edu.princeton.cs.introcs.*;
/* Code smells: 
 * **Functions Names should do what they say: but since this is such a huge method there 
 * are a lot of things going on. Renamed smaller methods so they accurately describe what is going on in the code.
 * 
 * **Encapsulate Conditionals: moved the conditionals that check for the skunks to their own methods checkForSingleSkunk, 
 * checkForDoubleSkunk, checkForSkunkDeuce
 * 
 * **Code should do one thing: changed so the responses to the specific special dice are separated out into their own methods. Also separated
 * out some code like setup and RollAgain, which is repeated several times.
 */
public class SkunkDomain
{
	public SkunkUI skunkUI;
	public UI ui;
	public int numberOfPlayers;
	public String[] playerNames;
	public ArrayList<Player> players;
	public int kitty;

	public Player activePlayer;
	public int activePlayerIndex;

	public boolean wantsToQuit;
	public boolean oneMoreRoll;

	public Dice skunkDice;

	public SkunkDomain(SkunkUI ui)
	{
		this.skunkUI = ui;
		this.ui = ui; // hide behind the interface UI
		
		this.playerNames = new String[20];
		this.players = new ArrayList<Player>();
		this.skunkDice = new Dice();
		this.wantsToQuit = false;
		this.oneMoreRoll = false;
	}

	public boolean run()
	{
		this.welcome();
		this.addPlayers();


		activePlayerIndex = 0;
		activePlayer = players.get(activePlayerIndex);

		ui.println("Starting game...\n");
		boolean gameNotOver = true;

		while (gameNotOver)
		{
			this.setupRoll();
			Boolean wantsToRoll = this.rollAgain();
			
			while (wantsToRoll)
			{
				this.startRoll();
				if (this.checkforDoubleSkunk())
				{
					this.isDoubleSkunk();
					wantsToRoll = false;
					break;
				}
				else if (this.checkforSkunkDeuce())
				{
					
					wantsToRoll = false;
					break;
				}
				else if (this.checkForSingleSkunk())
				{
					ui.println("One Skunk! You lose the turn, zeroing out the turn score and paying 1 chip to the kitty");
					kitty += 1;
					activePlayer.setNumberChips(activePlayer.getNumberChips() - 1);
					activePlayer.setTurnScore(0);
					wantsToRoll = false;
					break;

				}

				activePlayer.setRollScore(skunkDice.getLastRoll());
				activePlayer.setTurnScore(activePlayer.getTurnScore() + skunkDice.getLastRoll());
				ui.println(
						"Roll of " + skunkDice.toString() + ", gives new turn score of " + activePlayer.getTurnScore());
				wantsToRoll = rollAgain();
			}

			ui.println("End of turn for " + playerNames[activePlayerIndex]);
			ui.println("Score for this turn is " + activePlayer.getTurnScore() + ", added to...");
			ui.println("Previous game score of " + activePlayer.getGameScore());
			activePlayer.setGameScore(activePlayer.getGameScore() + activePlayer.getTurnScore());
			ui.println("Gives new game score of " + activePlayer.getGameScore());

			ui.println("");
			if (activePlayer.getGameScore() >= 100)
				gameNotOver = false;

			ui.println("Scoreboard: ");
			ui.println("Kitty has " + kitty + " chips.");
			ui.println("Player name -- Turn score -- Game score -- Total chips");
			ui.println("-----------------------");

			for (int i = 0; i < numberOfPlayers; i++)
			{
				ui.println(playerNames[i] + " -- " + players.get(i).getTurnScore() + " -- " + players.get(i).getGameScore()
						+ " -- " + players.get(i).getNumberChips());
			}
			ui.println("-----------------------");

			ui.println("Turn passes to right...");

			activePlayerIndex = (activePlayerIndex + 1) % numberOfPlayers;
			activePlayer = players.get(activePlayerIndex);

		}
		// last round: everyone but last activePlayer gets another shot


		 
		ui.println("**** Last turn for all... ****");

		for (int i = activePlayerIndex, count = 0; count < numberOfPlayers-1; i = (i++) % numberOfPlayers, count++)
		{
			ui.println("Last turn for player " + playerNames[activePlayerIndex] + "...");
			activePlayer.setTurnScore(0);

			Boolean wantsToRoll = rollAgain();

			while (wantsToRoll)
			{
				skunkDice.roll();
				ui.println("Roll is " + skunkDice.toString() + "\n");

				if (checkforDoubleSkunk())
				{
					wantsToRoll = false;
					break;
				}
				else if (checkforSkunkDeuce())
				{
					isSkunkDeuce();
					wantsToRoll = false;
				}
				else if (this.checkForSingleSkunk())
				{
					isOneSkunk();
					wantsToRoll = false;
				}
				else
				{
					activePlayer.setTurnScore(activePlayer.getRollScore() + skunkDice.getLastRoll());
					ui.println("Roll of " + skunkDice.toString() + ", giving new turn score of "
							+ activePlayer.getTurnScore());

					ui.println("Scoreboard: ");
					ui.println("Kitty has " + kitty);
					ui.println("Player name -- Turn score -- Game score -- Total chips");
					ui.println("-----------------------");

					for (int pNumber = 0; pNumber < numberOfPlayers; pNumber++)
					{
						ui.println(playerNames[pNumber] + " -- " + players.get(pNumber).turnScore + " -- "
								+ players.get(pNumber).getGameScore() + " -- " + players.get(pNumber).getNumberChips());
					}
					ui.println("-----------------------");

					wantsToRoll = rollAgain();
				}

			}

			activePlayer.setTurnScore(activePlayer.getRollScore() + skunkDice.getLastRoll());
			ui.println("Final roll of " + skunkDice.toString() + ", giving final game score of "
					+ activePlayer.getRollScore());

		}

		int winner = 0;
		int winnerScore = 0;

		for (int player = 0; player < numberOfPlayers; player++)
		{
			Player nextPlayer = players.get(player);
			ui.println("Final game score for " + playerNames[player] + " is " + nextPlayer.getGameScore());
			if (nextPlayer.getGameScore() > winnerScore)
			{
				winner = player;
				winnerScore = nextPlayer.getGameScore();
			}
		}

		ui.println(
				"Game winner is " + playerNames[winner] + " with score of " + players.get(winner).getGameScore());
		players.get(winner).setNumberChips(players.get(winner).getNumberChips() + kitty);
		ui.println("Game winner earns " + kitty + " chips , finishing with " + players.get(winner).getNumberChips());

		ui.println("\nFinal scoreboard for this game:");
		ui.println("Player name -- Game score -- Total chips");
		ui.println("-----------------------");

		for (int pNumber = 0; pNumber < numberOfPlayers; pNumber++)
		{
			ui.println(playerNames[pNumber] + " -- " + players.get(pNumber).getGameScore() + " -- "
					+ players.get(pNumber).getNumberChips());
		}

		ui.println("-----------------------");
		return true;
	}
	
	public void isDoubleSkunk() {
		ui.println("Two Skunks! You lose the turn, zeroing out both turn and game scores and paying 4 chips to the kitty");
		kitty += 4;
		activePlayer.setNumberChips(activePlayer.getNumberChips() - 4);
		activePlayer.setTurnScore(0);
		activePlayer.setGameScore(0);
	}
	
	public void isSkunkDeuce() {
		ui.println("Skunks and Deuce! You lose the turn, zeroing out the turn score and paying 2 chips to the kitty");
		kitty += 2;
		activePlayer.setNumberChips(activePlayer.getNumberChips() - 2);
		activePlayer.setTurnScore(0);
	}
	
	public void startRoll() {
		activePlayer.setRollScore(0);
		skunkDice.roll();
	}
	
	public void isOneSkunk() {
		ui.println("One Skunk!  You lose the turn, zeroing out the turn score and paying 1 chip to the kitty");
		kitty += 1;
		activePlayer.setNumberChips(activePlayer.getNumberChips() - 1);
		activePlayer.setTurnScore(0);
	}

	
	public Boolean checkforDoubleSkunk() {
		if (skunkDice.getLastRoll() == 2) {
			return true;
		}
		return false;
	}
	
	public Boolean checkforSkunkDeuce() {
		if (skunkDice.getLastRoll() == 3) {
			return true;
		}
		return false;
	}
	
	public Boolean checkForSingleSkunk() {
		if(skunkDice.getDie1().getLastRoll() == 1 || skunkDice.getDie2().getLastRoll() == 1) {
			return true;
		}
		return false;
	}
	
	public void welcome() {
		ui.println("Welcome to Skunk 0.47\n");

		String numberPlayersString = skunkUI.promptReadAndReturn("How many players?");
		this.numberOfPlayers = Integer.parseInt(numberPlayersString);
	}
	
	public void addPlayers() {
		for (int playerNumber = 0; playerNumber < numberOfPlayers; playerNumber++)
		{
			ui.print("Enter name of player " + (playerNumber + 1) + ": ");
			playerNames[playerNumber] = StdIn.readLine();
			this.players.add(new Player(50));
		}
	}
	
	public void setupRoll() {
		ui.println("Next player is " + playerNames[activePlayerIndex] + ".");
		activePlayer.setTurnScore(0);
	}
	
	public Boolean rollAgain() {
		String wantsToRollStr = ui.promptReadAndReturn("Roll? y or n");
		Boolean wantsToRoll = 'y' == wantsToRollStr.toLowerCase().charAt(0);
		return wantsToRoll;
	}

}
