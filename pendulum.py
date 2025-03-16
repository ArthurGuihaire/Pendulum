import pygame
import math
import numpy as np

pygame.init()

width, height = 2400,800
screen = pygame.display.set_mode((width, height))
pygame.display.set_caption("Inverted Pendulum Simulation")
clock = pygame.time.Clock()

g = 2
length = 300 # pixels

x = pygame.mouse.get_pos()[0]
y = height/2

angle = math.pi/4
angular_velocity = 0

user_speed = 0
user_newspeed  = 0

def update_speed(angle,angular_velocity,user_acceleration):
	angular_velocity += (g/length) * math.cos(angle)
	angular_velocity += (user_acceleration/length) * math.sin(angle)
	return angular_velocity

def draw_pendulum(x,angle):
	pygame.draw.line(screen, (255,255,255),(x,y),(x+length*math.cos(angle),y+length*math.sin(angle)))
	pygame.draw.circle(screen, (255,255,255),(x+length*math.cos(angle),y+length*math.sin(angle)),50)

pygame.event.set_blocked(pygame.MOUSEMOTION)

def simulate(angle):
	score = 0
	user_speed = 0
	angular_velocity = 0
	pygame.mouse.set_pos(width/2,height/2)
	for i in range(60):
		if angle>75 and angle<105:
			score += 1
		for j in range(60):
			# handle events
			for event in pygame.event.get():
				if event.type == pygame.QUIT:
					print("Score: "+str(score))
					pygame.quit()
					exit(0)

			user_newspeed = pygame.mouse.get_rel()[0]
			user_acceleration = user_newspeed - user_speed
			user_speed = user_newspeed
			x = pygame.mouse.get_pos()[0]

			angular_velocity = 0.995*update_speed(angle,angular_velocity,user_acceleration)
			angle += angular_velocity

			screen.fill((0,0,0))
			draw_pendulum(x,angle)
			pygame.display.flip()

			clock.tick(60)


print("Score: "+str(simulate(angle)))

pygame.quit()