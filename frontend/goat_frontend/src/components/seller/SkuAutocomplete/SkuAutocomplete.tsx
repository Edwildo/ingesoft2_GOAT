import React, { useState, useEffect, useRef } from 'react';
import { catalogService } from '../../../api/catalog.service';
import { Sneaker } from '../../../types/catalog.types';
import { Input } from '../../common/Input';
import styles from './SkuAutocomplete.module.css';

export interface SkuAutocompleteProps {
  value: string;
  onChange: (sku: string) => void;
  onSneakerSelect?: (sneaker: Sneaker) => void;
  error?: string;
  disabled?: boolean;
}

export const SkuAutocomplete: React.FC<SkuAutocompleteProps> = ({
  value,
  onChange,
  onSneakerSelect,
  error,
  disabled,
}) => {
  const [suggestions, setSuggestions] = useState<Sneaker[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [showSuggestions, setShowSuggestions] = useState(false);
  const [selectedIndex, setSelectedIndex] = useState(-1);
  const containerRef = useRef<HTMLDivElement>(null);
  const inputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (containerRef.current && !containerRef.current.contains(event.target as Node)) {
        setShowSuggestions(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  useEffect(() => {
    if (value.length >= 2) {
      const timer = setTimeout(() => {
        searchSneakers(value);
      }, 300);

      return () => clearTimeout(timer);
    } else {
      setSuggestions([]);
      setShowSuggestions(false);
    }
  }, [value]);

  const searchSneakers = async (query: string) => {
    setIsLoading(true);
    try {
      const response = await catalogService.searchSneakers({ search: query, size: 10 });
      if (response.success && response.data) {
        setSuggestions(response.data.sneakers);
        setShowSuggestions(response.data.sneakers.length > 0);
      }
    } catch (err) {
      // Silencioso, el autocompletado es opcional
    } finally {
      setIsLoading(false);
    }
  };

  const handleSelect = (sneaker: Sneaker) => {
    onChange(sneaker.sku);
    setShowSuggestions(false);
    if (onSneakerSelect) {
      onSneakerSelect(sneaker);
    }
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (!showSuggestions || suggestions.length === 0) return;

    if (e.key === 'ArrowDown') {
      e.preventDefault();
      setSelectedIndex((prev) => (prev < suggestions.length - 1 ? prev + 1 : prev));
    } else if (e.key === 'ArrowUp') {
      e.preventDefault();
      setSelectedIndex((prev) => (prev > 0 ? prev - 1 : -1));
    } else if (e.key === 'Enter' && selectedIndex >= 0) {
      e.preventDefault();
      handleSelect(suggestions[selectedIndex]);
    } else if (e.key === 'Escape') {
      setShowSuggestions(false);
    }
  };

  return (
    <div ref={containerRef} className={styles.container}>
      <Input
        ref={inputRef}
        id="sku"
        label="SKU del Sneaker"
        value={value}
        onChange={(e) => {
          onChange(e.target.value);
          setSelectedIndex(-1);
        }}
        onFocus={() => {
          if (suggestions.length > 0) {
            setShowSuggestions(true);
          }
        }}
        onKeyDown={handleKeyDown}
        error={error}
        disabled={disabled}
        fullWidth
        helperText="Busca por SKU, marca o modelo"
      />

      {isLoading && (
        <div className={styles.loading}>
          <span>Buscando...</span>
        </div>
      )}

      {showSuggestions && suggestions.length > 0 && (
        <div className={styles.suggestions}>
          {suggestions.map((sneaker, index) => (
            <div
              key={sneaker.sku}
              className={`${styles.suggestionItem} ${
                index === selectedIndex ? styles.selected : ''
              }`}
              onClick={() => handleSelect(sneaker)}
              onMouseEnter={() => setSelectedIndex(index)}
            >
              <div className={styles.suggestionContent}>
                <div className={styles.suggestionBrand}>{sneaker.brand}</div>
                <div className={styles.suggestionModel}>{sneaker.model}</div>
                <div className={styles.suggestionSku}>SKU: {sneaker.sku}</div>
              </div>
              {sneaker.media?.coverImage && (
                <img
                  src={sneaker.media.coverImage}
                  alt={sneaker.model}
                  className={styles.suggestionImage}
                />
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

