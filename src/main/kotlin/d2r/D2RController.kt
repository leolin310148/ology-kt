package d2r

import extension.log
import extension.wait
import external.nuttree.Key
import external.nuttree.keyboard
import external.wincontrol.WinControl.Window
import kotlinx.coroutines.await
import kotlinx.coroutines.delay

object D2RController {
    fun isGameRunning(switchToForegroundWhenRunning: Boolean = false): Boolean {
        val d2r = Window.getByTitle(gameWindowTitle)
        if (switchToForegroundWhenRunning && d2r != null) {
            d2r.setForeground()
        }
        return d2r != null
    }

    suspend fun isInGame() = ScreenController.oneOfImagesIn(
        listOf(
            "in_game_menu_bar.png",
            "left_blood_ball.png",
            "right_mana_ball.png"
        )
    ) != null

    suspend fun exitGame() {
        keyboard.pressKey(Key.Escape).await()
        keyboard.releaseKey(Key.Escape).await().wait(100)
        MouseController.clickRelativeXY(x = exitGameX, y = exitGameY)
    }

    suspend fun makeGame(name: String, password: String) {
        if (isGameRunning(switchToForegroundWhenRunning = true)) {
            MouseController.clickRelativeXY(exitGameX, exitGameY).wait(500)
            isInGame()
                .takeIf { it }
                .run { exitGame().wait(1500).log("inGame, exiting game") }

            MouseController.clickRelativeXY(makeGameTabX, makeGameTabY).wait(1000).log("game name=$name, password=$password")
            MouseController.clickRelativeXY(x = hostGameNameInputX, y = hostGameNameInputY)
            KeyboardController.inputGameNameAndPassword(name = name, password = password).wait(1000)
            KeyboardController.submitGameForm().wait(3000)
            if (isInGame()) {
                log("Game $name created")
            } else {
                log("Failed to create game, something wrong...")
            }
        } else {
            log("D2R not running, please launch D2R and go to lobby first")
        }
    }

    suspend fun joinGame(name: String, password: String) {
        if (isGameRunning()) {
            isInGame()
                .takeIf { it }
                .run { exitGame() }
                .log("inGame, exiting game")
                .wait(1500)
        } else {
            log("D2R not running, please launch D2R and go to lobby first")
        }
        MouseController.clickRelativeXY(x = joinGameNameInputX, y = joinGameNameInputY).wait(100)
        KeyboardController.inputGameNameAndPassword(name = name, password = password).wait(100)
        MouseController.clickRelativeXY(x = refreshBtnX, y = refreshBtnY).wait(1000)
        KeyboardController.submitGameForm().wait(3000)
        if (isInGame()) {
            log("Game $name joined")
        } else {
            log("Did not detect in game or not...")
        }
    }
}
