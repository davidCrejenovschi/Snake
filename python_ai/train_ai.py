import gymnasium as gym
from gymnasium import spaces
import numpy as np
import socket
import json
import os
from stable_baselines3 import PPO
from stable_baselines3.common.monitor import Monitor

class SnakeJavaRadarEnv(gym.Env):
    def __init__(self):
        super(SnakeJavaRadarEnv, self).__init__()

        # 4 acțiuni: 0=Sus, 1=Jos, 2=Stânga, 3=Dreapta (adaptează dacă ale tale sunt altfel)
        self.action_space = spaces.Discrete(4)

        # ACUM AVEM 24 DE INTRĂRI! Toate sunt valori între 0.0 și 1.0
        self.observation_space = spaces.Box(low=0.0, high=1.0, shape=(24,), dtype=np.float32)

        self.server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.server_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        self.server_socket.bind(('127.0.0.1', 65432))
        self.server_socket.listen()
        self.conn = None

    def reset(self, seed=None, options=None):
        super().reset(seed=seed)
        if self.conn: self.conn.close()
        self.conn, addr = self.server_socket.accept()

        data = self.conn.recv(2048) # Mărim buffer-ul pentru că JSON-ul e mai lung acum
        if not data: return np.zeros(24, dtype=np.float32), {}

        state_dict = json.loads(data.decode('utf-8').strip())

        # Luăm lista "radar" direct din JSON și o facem Numpy Array
        obs = np.array(state_dict['radar'], dtype=np.float32)
        return obs, {}

    def step(self, action):
        self.conn.sendall(f"{action}\n".encode('utf-8'))
        data = self.conn.recv(2048)

        if not data: return np.zeros(24, dtype=np.float32), 0, True, False, {}

        state_dict = json.loads(data.decode('utf-8').strip())
        obs = np.array(state_dict['radar'], dtype=np.float32)

        terminated = state_dict['game_over']

        # Sistemul de Recompense
        reward = 0
        if terminated:
            reward = -10.0
        elif state_dict['ate_food']:
            reward = 10.0
        else:
            reward = -0.01 # Penalizare mică ca să nu se învârtă în cerc

        return obs, reward, terminated, False, {}

# --- MOTORUL DE ANTRENAMENT ---
if __name__ == "__main__":
    log_dir = "logs/tensorboard/"
    os.makedirs(log_dir, exist_ok=True)

    # 1. Inițializăm mediul cu Radar
    raw_env = SnakeJavaRadarEnv()
    env = Monitor(raw_env, log_dir)

    # 2. VERIFICĂM DACĂ EXISTĂ UN CREIER VECHI
    model_path = "expert_snake_radar.zip"
    if os.path.exists(model_path):
        print("[*] SUPER! Am găsit creierul vechi. Continuăm studiile de unde am rămas...")
        # Încărcăm modelul și îl conectăm la noul mediu
        model = PPO.load("expert_snake_radar", env=env, tensorboard_log=log_dir)
    else:
        print("[*] Nu am găsit creierul vechi. Începem antrenamentul de la ZERO...")
        model = PPO("MlpPolicy", env, verbose=1, tensorboard_log=log_dir)

    print("--- START ANTRENAMENT RADAR (500k PAȘI) ---")

    # Antrenăm! 500.000 de pași ar trebui să fie suficienți ca să vezi un geniu acum.
    model.learn(total_timesteps=500000, reset_num_timesteps=False)

    print("--- ANTRENAMENT FINALIZAT ---")
    model.save("expert_snake_radar")
    print("Noul creier a fost salvat ca 'expert_snake_radar.zip'!")