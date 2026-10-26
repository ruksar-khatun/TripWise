/**
 * TripWise AI Travel Assistant - Secure Reference Backend Proxy
 *
 * Instructions:
 * 1. cd server
 * 2. npm install
 * 3. Set GEMINI_API_KEY in server/.env
 *    GEMINI_API_KEY=your_key_here
 *    PORT=3000
 * 4. Run `npm start`
 */

require('dotenv').config();
const express = require('express');
const cors = require('cors');
const rateLimit = require('express-rate-limit');
const { GoogleGenerativeAI } = require('@google/generative-ai');

const app = express();

app.use(cors());
app.use(express.json({ limit: '1mb' }));

// Rate Limiter: Max 30 requests per minute per IP
const apiLimiter = rateLimit({
  windowMs: 60 * 1000,
  max: 30,
  message: { error: 'Too many requests. Please wait a moment before asking again.' },
  standardHeaders: true,
  legacyHeaders: false
});

app.use('/api/', apiLimiter);

const PORT = process.env.PORT || 3000;
const GEMINI_API_KEY = process.env.GEMINI_API_KEY;

// Native System Instruction for TripWise Assistant
const SYSTEM_INSTRUCTION = `You are the AI Travel Assistant inside TripWise, a smart travel planner app specializing in 30+ Asian destinations (such as Munnar, Goa, Jaipur, Tokyo, Bali, Bangkok, Seoul, Dubai, Maldives, etc.).

Your Goal:
- Provide helpful, friendly, concise, and structured travel recommendations, 3-day itineraries, local food tips, and budget estimates.
- Use the user's selected currency and travel preferences provided in context.
- Treat generated itineraries and budget breakdowns as estimates.
- Do NOT invent live flight availability, exact opening hours, or real-time exchange rates.
- Structure responses clearly with bullet points and emojis.`;

app.get('/health', (req, res) => {
  res.json({
    status: 'ok',
    service: 'TripWise AI Backend',
    apiKeyConfigured: !!(GEMINI_API_KEY && GEMINI_API_KEY.trim().length > 0)
  });
});

app.post('/api/chat', async (req, res) => {
  try {
    const { message, conversationHistory = [], context = {} } = req.body;

    if (!message || message.trim() === '') {
      return res.status(400).json({ error: 'Message payload cannot be empty.' });
    }

    if (message.length > 2000) {
      return res.status(400).json({ error: 'Message length exceeds 2000 characters limit.' });
    }

    if (!GEMINI_API_KEY || GEMINI_API_KEY.trim() === '') {
      return res.status(500).json({
        error: 'Backend GEMINI_API_KEY is missing. Please set GEMINI_API_KEY in server/.env file or environment variables.'
      });
    }

    const genAI = new GoogleGenerativeAI(GEMINI_API_KEY.trim());

    // Verified models confirmed via ListModels API for this key
    const candidateModels = [
      'gemini-flash-lite-latest',
      'gemini-flash-latest',
      'gemini-pro-latest'
    ];

    const contextPrompt = `[TripWise User Context: Currency: ${context.selectedCurrency || 'USD'}, Preferences: ${context.userInterests ? context.userInterests.join(', ') : 'General'}, Travel Style: ${context.travelStyle || 'Balanced'}, Destination: ${context.selectedDestinationName || 'None'}]

User Query: ${message}`;

    // Build alternating contents array for Gemini SDK
    const filteredHistory = [];
    conversationHistory.forEach(item => {
      const role = (item.role === 'user') ? 'user' : 'model';
      if (filteredHistory.length === 0 || filteredHistory[filteredHistory.length - 1].role !== role) {
        filteredHistory.push({
          role: role,
          parts: [{ text: item.content }]
        });
      }
    });

    if (filteredHistory.length > 0 && filteredHistory[filteredHistory.length - 1].role === 'user') {
      filteredHistory.pop();
    }

    const contents = [...filteredHistory, {
      role: 'user',
      parts: [{ text: contextPrompt }]
    }];

    let replyText = null;
    let lastError = null;

    for (const modelName of candidateModels) {
      for (let attempt = 1; attempt <= 2; attempt++) {
        try {
          const model = genAI.getGenerativeModel({
            model: modelName,
            systemInstruction: SYSTEM_INSTRUCTION
          });
          const result = await model.generateContent({ contents });
          const response = await result.response;
          replyText = response.text();
          if (replyText && replyText.length > 0) {
            break;
          }
        } catch (e) {
          lastError = e;
          if (attempt < 2 && e.message && e.message.includes('503')) {
            await new Promise(r => setTimeout(r, 1500));
          }
        }
      }
      if (replyText) break;
    }

    if (replyText) {
      return res.json({
        reply: replyText,
        status: 'success'
      });
    } else {
      const sanitizedMsg = lastError ? lastError.message.split('\n')[0] : 'All model attempts failed.';
      return res.status(500).json({
        error: `Gemini API Error: ${sanitizedMsg}`
      });
    }

  } catch (error) {
    const sanitizedErrorMsg = error ? error.message.split('\n')[0] : 'Unknown server error';
    console.error('TripWise AI Backend Error:', sanitizedErrorMsg);
    return res.status(500).json({
      error: `Server Error processing AI request: ${sanitizedErrorMsg}`
    });
  }
});

app.listen(PORT, () => {
  console.log(`TripWise AI Backend listening on port ${PORT}`);
  console.log(`Endpoint URL: http://localhost:${PORT}/api/chat (For Android Emulator: http://10.0.2.2:${PORT}/api/chat)`);
  if (!GEMINI_API_KEY) {
    console.warn('⚠️ WARNING: GEMINI_API_KEY is not set in environment variables! Set GEMINI_API_KEY in server/.env');
  }
});
