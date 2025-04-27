
from ClientSocket import start
from constants import COMMANDS


def show_menu():
    """Displays the main command menu to the user."""
    print("\nAvailable commands:")
    print("1 - Open the camera")
    print("2 - Take a photo")
    print("3 - Get device properties")
    print("4 - Exit")


def message_input():
    """Handles user input and triggers the appropriate actions."""
    while True:
        show_menu()
        user_input = input(">> ").strip()

        if user_input in ['4', 'exit']:
            print(" Exiting...")
            break
        elif user_input in COMMANDS:
            description, command = COMMANDS[user_input]
            print(description)
            start(command)
        else:
            print("Unknown command. Please try again.")


if __name__ == "__main__":
    print("Client started.")
    message_input()
