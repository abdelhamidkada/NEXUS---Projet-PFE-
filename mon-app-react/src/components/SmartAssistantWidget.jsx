import React, { useState, useRef, useEffect } from 'react';
import {
  MessageSquare,
  Bot,
  X,
  Send,
  Sparkles,
  User,
  Loader2
} from 'lucide-react';

// ---------------------------------------------------------------------------
// Mock Data (Initial Conversation)
// ---------------------------------------------------------------------------
const INITIAL_MESSAGES = [
  {
    id: 1,
    sender: 'bot',
    text: "Bonjour ! Je suis l'assistant RH NEXUS. Comment puis-je vous aider aujourd'hui ?",
    time: '10:00'
  },
  {
    id: 2,
    sender: 'user',
    text: "Quel est mon solde de congés ?",
    time: '10:01'
  },
  {
    id: 3,
    sender: 'bot',
    text: "J'ai consulté votre dossier. Il vous reste 14 jours de congés annuels. Souhaitez-vous que je prépare une demande pour vous ?",
    time: '10:01'
  }
];

// ---------------------------------------------------------------------------
// Main Component
// ---------------------------------------------------------------------------
export default function SmartAssistantWidget() {
  const [isOpen, setIsOpen] = useState(false);
  const [messages, setMessages] = useState(INITIAL_MESSAGES);
  const [inputValue, setInputValue] = useState('');
  const [isTyping, setIsTyping] = useState(false);
  
  const messagesEndRef = useRef(null);

  // Auto scroll to bottom when new messages arrive
  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    if (isOpen) {
      scrollToBottom();
    }
  }, [messages, isOpen]);

  const handleSend = (e) => {
    e.preventDefault();
    if (!inputValue.trim()) return;

    const userTime = new Date().toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });
    
    // 1. Add user message
    const userMsg = {
      id: Date.now(),
      sender: 'user',
      text: inputValue,
      time: userTime
    };

    setMessages(prev => [...prev, userMsg]);
    const query = inputValue;
    setInputValue('');

    // 2. Trigger Bot Auto-Response
    setIsTyping(true);
    setTimeout(() => {
      setIsTyping(false);
      const botTime = new Date().toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });
      
      const botMsg = {
        id: Date.now() + 1,
        sender: 'bot',
        text: `Je transfère votre demande au service RH (Création de ticket en cours...). Notre équipe traitera : "${query}"`,
        time: botTime
      };

      setMessages(prev => [...prev, botMsg]);
    }, 1000);
  };

  return (
    <div className="fixed bottom-6 right-6 z-50 font-sans">
      
      {/* 1. Toggle Button */}
      {!isOpen && (
        <button
          onClick={() => setIsOpen(true)}
          className="relative group w-14 h-14 rounded-full bg-gradient-to-tr from-blue-600 to-indigo-600 hover:from-blue-500 hover:to-indigo-500 text-white flex items-center justify-center shadow-2xl transition-all duration-300 transform hover:scale-105 active:scale-95 border-2 border-white focus:outline-none focus:ring-4 focus:ring-blue-100"
        >
          {/* Radial ping effect */}
          <span className="absolute inset-0 rounded-full bg-blue-500/30 animate-ping opacity-75" />
          
          <Bot className="relative h-6 w-6 shrink-0" />
          
          {/* Sparkles effect */}
          <span className="absolute -top-1.5 -right-1.5 bg-indigo-500 text-white rounded-full p-1 border border-white text-[9px] shadow-sm animate-bounce">
            <Sparkles className="h-2.5 w-2.5" />
          </span>
        </button>
      )}

      {/* 2. Chat Window */}
      {isOpen && (
        <div className="w-[360px] max-w-[calc(100vw-2rem)] h-[480px] bg-white border border-gray-200/80 rounded-3xl shadow-2xl flex flex-col overflow-hidden animate-in fade-in slide-in-from-bottom-6 duration-200">
          
          {/* Header */}
          <div className="px-5 py-4 bg-gradient-to-r from-blue-600 to-indigo-600 text-white flex items-center justify-between shadow-sm relative">
            <div className="absolute -top-6 -right-6 w-24 h-24 bg-white/10 rounded-full blur-2xl pointer-events-none" />
            
            <div className="flex items-center gap-2.5 z-10">
              <div className="p-1.5 bg-white/15 rounded-xl border border-white/10">
                <Bot className="h-5 w-5 text-white" />
              </div>
              <div>
                <h3 className="text-sm font-extrabold tracking-tight">NEXUS AI Assistant</h3>
                <p className="text-[10px] text-emerald-300 font-bold flex items-center gap-1 mt-0.5">
                  <span className="h-1.5 w-1.5 rounded-full bg-emerald-400 animate-pulse" />
                  En ligne
                </p>
              </div>
            </div>
            
            <button
              onClick={() => setIsOpen(false)}
              className="p-1.5 rounded-xl bg-white/10 hover:bg-white/20 text-white/80 hover:text-white transition-all duration-150 z-10"
            >
              <X className="h-4 w-4" />
            </button>
          </div>

          {/* Messages Area */}
          <div className="flex-1 overflow-y-auto p-4 bg-slate-50/50 space-y-4">
            {messages.map((msg) => {
              const isBot = msg.sender === 'bot';
              return (
                <div
                  key={msg.id}
                  className={`flex gap-2.5 max-w-[85%] ${isBot ? 'mr-auto' : 'ml-auto flex-row-reverse'}`}
                >
                  {/* Sender Avatar */}
                  <div
                    className={`h-7 w-7 rounded-xl flex items-center justify-center shrink-0 border shadow-sm text-xs font-bold
                      ${isBot 
                        ? 'bg-blue-50 border-blue-100 text-blue-600' 
                        : 'bg-indigo-50 border-indigo-100 text-indigo-600'}`}
                  >
                    {isBot ? <Bot className="h-4 w-4" /> : <User className="h-4 w-4" />}
                  </div>

                  {/* Message bubble */}
                  <div className="flex flex-col gap-1">
                    <div
                      className={`px-3.5 py-2.5 rounded-2xl text-xs font-medium leading-relaxed
                        ${isBot
                          ? 'bg-white text-gray-800 border border-gray-100 rounded-tl-none shadow-sm'
                          : 'bg-gradient-to-tr from-blue-600 to-indigo-600 text-white rounded-tr-none shadow-md'}`}
                    >
                      {msg.text}
                    </div>
                    <span className={`text-[9px] text-gray-400 font-semibold px-1 ${isBot ? 'text-left' : 'text-right'}`}>
                      {msg.time}
                    </span>
                  </div>
                </div>
              );
            })}

            {/* Typing indicator */}
            {isTyping && (
              <div className="flex gap-2.5 max-w-[80%] mr-auto">
                <div className="h-7 w-7 rounded-xl bg-blue-50 border border-blue-100 text-blue-600 flex items-center justify-center shrink-0 shadow-sm">
                  <Bot className="h-4 w-4" />
                </div>
                <div className="flex items-center gap-1.5 px-4 py-3 bg-white border border-gray-100 rounded-2xl rounded-tl-none shadow-sm">
                  <Loader2 className="h-3.5 w-3.5 animate-spin text-blue-600" />
                  <span className="text-xs text-gray-400 font-medium">NEXUS AI réfléchit...</span>
                </div>
              </div>
            )}
            <div ref={messagesEndRef} />
          </div>

          {/* Form / Input */}
          <form onSubmit={handleSend} className="p-3 bg-white border-t border-gray-100 flex gap-2">
            <input
              type="text"
              value={inputValue}
              onChange={(e) => setInputValue(e.target.value)}
              placeholder="Posez votre question (ex: congé, solde...)"
              className="flex-1 px-3 py-2 text-xs border border-gray-200 rounded-xl outline-none focus:border-blue-400 focus:ring-2 focus:ring-blue-500/10 transition-all bg-gray-50/50"
            />
            <button
              type="submit"
              disabled={!inputValue.trim() || isTyping}
              className="p-2 rounded-xl bg-blue-600 hover:bg-blue-500 disabled:bg-blue-300 text-white transition-all active:scale-95"
            >
              <Send className="h-3.5 w-3.5" />
            </button>
          </form>

        </div>
      )}

    </div>
  );
}
