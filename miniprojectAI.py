# SmartSnake: AI-Powered Snake Game with Intelligent Path Optimization
# Developed by Isra Rahmath K 💖

import pygame, sys, random
pygame.init()

# ----- GAME WINDOW SETTINGS -----
WIDTH, HEIGHT = 600, 400
CELL_SIZE = 20
win = pygame.display.set_mode((WIDTH, HEIGHT))
pygame.display.set_caption("SmartSnake: AI Snake Game 🐍")

# ----- COLORS -----
BLACK = (0, 0, 0)
GREEN = (0, 255, 0)
RED = (255, 0, 0)
WHITE = (255, 255, 255)

clock = pygame.time.Clock()
font = pygame.font.SysFont("Arial", 24)

# ----- DRAW FUNCTIONS -----
def draw_snake(snake):
    for pos in snake:
        pygame.draw.rect(win, GREEN, pygame.Rect(pos[0], pos[1], CELL_SIZE, CELL_SIZE))

def draw_food(food):
    pygame.draw.rect(win, RED, pygame.Rect(food[0], food[1], CELL_SIZE, CELL_SIZE))

def draw_score(score):
    text = font.render("Score: " + str(score), True, WHITE)
    win.blit(text, [10, 10])

# ----- RANDOM FOOD GENERATION -----
def food_position():
    return [random.randrange(1, (WIDTH // CELL_SIZE)) * CELL_SIZE,
            random.randrange(1, (HEIGHT // CELL_SIZE)) * CELL_SIZE]

# ----- SIMPLE AI LOGIC -----
# Snake moves toward food using a greedy approach
def ai_move(snake_head, food):
    x, y = snake_head
    fx, fy = food
    dx, dy = 0, 0
    if abs(fx - x) > abs(fy - y):
        dx = CELL_SIZE if fx > x else -CELL_SIZE
    else:
        dy = CELL_SIZE if fy > y else -CELL_SIZE
    return [dx, dy]

# ----- MAIN GAME LOOP -----
def game_loop(ai_mode=False):
    snake = [[100, 100], [80, 100], [60, 100]]
    direction = [CELL_SIZE, 0]  # Start moving right
    food = food_position()
    score = 0

    running = True
    while running:
        # --- EVENT HANDLING ---
        for event in pygame.event.get():
            if event.type == pygame.QUIT:
                pygame.quit()
                sys.exit()

        # --- USER INPUT ---
        if not ai_mode:
            keys = pygame.key.get_pressed()
            if keys[pygame.K_UP] and direction != [0, CELL_SIZE]:
                direction = [0, -CELL_SIZE]
            elif keys[pygame.K_DOWN] and direction != [0, -CELL_SIZE]:
                direction = [0, CELL_SIZE]
            elif keys[pygame.K_LEFT] and direction != [CELL_SIZE, 0]:
                direction = [-CELL_SIZE, 0]
            elif keys[pygame.K_RIGHT] and direction != [-CELL_SIZE, 0]:
                direction = [CELL_SIZE, 0]
        else:
            direction = ai_move(snake[0], food)  # AI decision

        # --- MOVE SNAKE ---
        new_head = [snake[0][0] + direction[0], snake[0][1] + direction[1]]
        snake.insert(0, new_head)

        # --- FOOD COLLISION ---
        if new_head == food:
            score += 1
            food = food_position()
        else:
            snake.pop()

        # --- COLLISION DETECTION ---
        if (new_head in snake[1:]) or new_head[0] < 0 or new_head[1] < 0 or new_head[0] >= WIDTH or new_head[1] >= HEIGHT:
            print("💀 Game Over! Final Score:", score)
            running = False

        # --- DRAW EVERYTHING ---
        win.fill(BLACK)
        draw_snake(snake)
        draw_food(food)
        draw_score(score)
        pygame.display.flip()

        clock.tick(10)  # Game speed (10 FPS)

# ----- RUN THE GAME -----
# To play manually → game_loop(ai_mode=False)
# To let AI play → game_loop(ai_mode=True)

if __name__ == "__main__":
    print("🎮 SmartSnake AI Game Started!")
    print("Press arrow keys for manual mode or toggle ai_mode=True for AI mode 🧠🐍")
    game_loop(ai_mode=True)
