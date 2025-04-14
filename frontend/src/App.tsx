import React, {useEffect, useState, useRef} from 'react';
import './App.css';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faTrash } from '@fortawesome/free-solid-svg-icons';

interface Task {
    id: number;
    title: string;
    completed: boolean;
    }

const API_URL = 'http://localhost:8080/tasks';
const WS_URL = 'ws://localhost:8080/ws/tasks';

const App: React.FC = () => {
    const [tasks, setTasks] = useState<Task[]>([]);
    const [title, setTitle] = useState('');
    const wsRef = useRef<WebSocket | null>(null);

    //pobierz zadania przy załadowaniu
    useEffect(() => {
        console.log('useEffect started at', new Date().toISOString());
        fetchTasks();
    
        const connectWebSocket = () => {
            if (!wsRef.current || wsRef.current.readyState === WebSocket.CLOSED) {
                console.log('Attempting to connect WebSocket to', WS_URL);
                const ws = new WebSocket(WS_URL);
                wsRef.current = ws;
    
                ws.onopen = () => {
                    console.log('WebSocket connected successfully at', new Date().toISOString());
                };
                ws.onmessage = (event) => {
                    console.log('WebSocket message received:', event.data);
                    const data = JSON.parse(event.data);
                    if (data.event === 'taskUpdated' || data.event === 'taskDeleted') {
                        console.log('Task updated or deleted, fetching tasks');
                        fetchTasks();
                    }
                };
                ws.onerror = (error) => {
                    console.error('WebSocket error:', error);
                };
                ws.onclose = (event) => {
                    console.log('WebSocket closed with code:', event.code, 'reason:', event.reason, 'at', new Date().toISOString());
                    if (event.code !== 1000) {
                        console.log('Reconnecting WebSocket in 1s');
                        setTimeout(connectWebSocket, 1000);
                    }
                };
            } else {
                console.log('WebSocket already exists, state:', wsRef.current.readyState);
            }
        };
    
        const initialTimeout = setTimeout(connectWebSocket, 100); // Opóźnienie 100ms
    
        return () => {
            console.log('Cleaning up WebSocket at', new Date().toISOString());
            clearTimeout(initialTimeout);
            if (wsRef.current && wsRef.current.readyState !== WebSocket.CLOSED) {
                wsRef.current.close();
            }
        };
    }, []);

    const fetchTasks = async () =>{
        try {
            const response = await fetch(API_URL);
            const data = await response.json();
            setTasks(data);
        } catch(error) {
                console.error('Błąd przy pobieraniu zadań: ', error)
        }
    };

    const handleSubmit = async (e: React.FormEvent) =>{
        e.preventDefault();
        const task = {id: Date.now(), title, completed: false};

        try {
            await fetch(API_URL, {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(task),
            });
            setTitle(''); // Wyczyść pole
        } catch (error){
                console.error('Błąd przy dodawaniu zadania:', error);
        }
    };

    const handleToggleComplete = async (id: number, completed: boolean) => {
        try {
            await fetch(`${API_URL}/${id}/status`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json'},
                body: JSON.stringify({ completed: !completed }),
            });
        } catch (error){
            console.error('Błąd przy aktualizacji zadania: ',error)
        }
    }

    const handleDelete = async (id: number) => {
        try{
            await fetch(`${API_URL}/${id}`, {
                method: 'DELETE'
            })
        } catch (error) {
            console.error('Błąd przy usuwaniu zadania: ', error)
        }
    }


    return(
    <div className="app">
        <div className='app-container'>
            <h1>Lista Zadań</h1>
            <form onSubmit={handleSubmit} className="task-form">
                <input
                    type="text"
                    value={title}
                    onChange={(e) => setTitle(e.target.value)}
                    placeholder="Wpisz zadanie"
                    required
                    className="task-input"
                    data-testid="task-input"
                />
                <button
                    type="submit"
                    className="task-button"
                    data-testid="add-task-button"
                >
                    Dodaj
                </button>
            </form>
            <ul className='task-list'>
                {tasks.map((task)=>(
                    <li
                        key={task.id}
                        className={`task-item ${task.completed ? 'completed' : ''}`}
                        data-testid={`task-${task.id}`}
                    >
                        <input
                            type="checkbox"
                            checked={task.completed}
                            onChange={() => handleToggleComplete(task.id, task.completed)}
                            data-testid={`task-checkbox-${task.id}`}
                        />
                        <span>{task.title}</span>
                        (Zakończone: {task.completed ? 'Tak' : 'Nie'})
                        <button
                            className='dlete-button'
                            onClick={() => handleDelete(task.id)}
                            data-testid={`delete-task-${task.id}`}
                        >
                                <FontAwesomeIcon icon={faTrash} />
                        </button>
                    </li>
                    ))}
            </ul>
        </div>
    </div>
    );
};

export default App;
