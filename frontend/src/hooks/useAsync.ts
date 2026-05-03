import { useState, useCallback } from 'react'

interface LoadingState {
    loading: boolean
    error: string | null
}

export const useAsync = (asyncFunction: () => Promise<any>) => {
    const [state, setState] = useState<LoadingState>({
        loading: false,
        error: null,
    })

    const execute = useCallback(async () => {
        setState({ loading: true, error: null })
        try {
            const response = await asyncFunction()
            setState({ loading: false, error: null })
            return response
        } catch (error: any) {
            setState({
                loading: false,
                error: error?.response?.data?.message || error?.message || 'Une erreur est survenue',
            })
            throw error
        }
    }, [asyncFunction])

    return { ...state, execute }
}
