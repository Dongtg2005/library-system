import { useState, useEffect } from 'react';

// Custom Hook để delay tự động API sau khi người dùng dừng nhập văn bản (giảm tải Server).
export default function useDebounce(value, delay) {
    const [debouncedValue, setDebouncedValue] = useState(value);

    useEffect(() => {
        const handler = setTimeout(() => {
            setDebouncedValue(value);
        }, delay);

        return () => {
            clearTimeout(handler);
        };
    }, [value, delay]);

    return debouncedValue;
}
