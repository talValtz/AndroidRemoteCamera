import socket

from constants import HOST, PORT


def start(message):
    """Establishes a connection to the server, sends a command, and handles the response."""
    try:
        client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        client_socket.connect((HOST, PORT))
        print("Connected To Server.")

        client_socket.send((message + '\n').encode('utf-8'))
        print(f"Message sent: {message}")

        client_file = client_socket.makefile('rb')
        first_response = client_file.readline().decode('utf-8').strip()

        if first_response == "IMAGE":
            open_image(client_file)
        else:
            read_props(client_file)
        client_socket.close()

    except ConnectionRefusedError:
        print("Connection failed. Make sure the server is running.")

    except Exception as e:
        print(f"Error: {e}")


def open_image(client_file):
    """Receives an image file from the server and saves it locally."""
    print("Receiving an image...")
    file_size_line = client_file.readline().decode('utf-8').strip()
    file_size = int(file_size_line)
    received_data = client_file.read(file_size)
    with open('received_image.jpg', 'wb') as f:
        f.write(received_data)
    print("Image saved as 'received_image.jpg'")


def read_props(client_file):
    """Receives a text response from the server and prints it."""
    print(" Receiving device properties...")
    props = ""
    while True:
        line = client_file.readline()
        if not line:
            break
        props += line.decode('utf-8')
    print("Properties received:")
    print(props)
