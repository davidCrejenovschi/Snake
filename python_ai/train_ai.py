import gymnasium as gym
from gymnasium import spaces
import numpy as np
import socket
import json
from stable_baselines3 import PPO

class SnakeJavaEnv(gym.Env):
    """Mediu customizat care comunică cu jocul tău Java"""

    def __init__(self):
        super(SnakeJavaEnv, self).__init__()

        # 4 acțiuni posibile (0=Sus, 1=Jos, 2=Stânga, 3=Dreapta)
        self.action_space = spaces.Discrete(4)

        # Ce vede AI-ul: 4 numere (head_x, head_y, food_x, food_y).
        # Presupunem o grilă de maxim 100x100
        self.observation_space = spaces.Box(low=0, high=100, shape=(4,), dtype=np.float32)

        # Setup Server Socket
        self.server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.server_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        self.server_socket.bind(('127.0.0.1', 65432))
        self.server_socket.listen()

        self.conn = None

    def reset(self, seed=None, options=None):
        """Se apelează la începutul fiecărui meci nou"""
        super().reset(seed=seed)

        if self.conn:
            self.conn.close()

        # Așteptăm ca Java să pornească un meci nou
        self.conn, addr = self.server_socket.accept()

        # Citim prima stare a tablei
        data = self.conn.recv(1024)
        state_dict = json.loads(data.decode('utf-8').strip())

        obs = np.array([state_dict['head_x'], state_dict['head_y'],
                        state_dict['food_x'], state_dict['food_y']], dtype=np.float32)
        return obs, {}

    def step(self, action):
        """Trimite mutarea către Java și analizează rezultatul"""
        # Trimite acțiunea către Java
        self.conn.sendall(f"{action}\n".encode('utf-8'))

        # Așteaptă răspunsul (noua stare)
        data = self.conn.recv(1024)
        if not data:
            # Dacă Java s-a deconectat brusc
            return np.zeros(4, dtype=np.float32), 0, True, False, {}

        state_dict = json.loads(data.decode('utf-8').strip())

        obs = np.array([state_dict['head_x'], state_dict['head_y'],
                        state_dict['food_x'], state_dict['food_y']], dtype=np.float32)

        terminated = state_dict['game_over']

        # --- SISTEMUL DE RECOMPENSE (Aici învață AI-ul!) ---
        reward = 0
        if terminated:
            reward = -10.0  # Pedeapsă uriașă pentru că a intrat în zid
        elif state_dict['ate_food']:
            reward = 10.0   # Recompensă mare pentru că a mâncat
        else:
            reward = -0.01  # Pedeapsă mică pentru fiecare pas în gol (ca să îl forțăm să se grăbească)

        return obs, reward, terminated, False, {}

if __name__ == "__main__":
    print("--- PREGĂTIRE MEDIU DE ANTRENAMENT ---")
    env = SnakeJavaEnv()

    # Inițializăm algoritmul PPO
    model = PPO("MlpPolicy", env, verbose=1)

    print("--- START ANTRENAMENT ---")
    print("Pornește AiClient.main() din Java acum!")

    # AI-ul va juca 100.000 de mutări (aproximativ câteva sute/mii de meciuri)
    model.learn(total_timesteps=100000)

    print("--- ANTRENAMENT FINALIZAT ---")
    model.save("expert_snake")
    print("Creierul a fost salvat în 'expert_snake.zip'!")